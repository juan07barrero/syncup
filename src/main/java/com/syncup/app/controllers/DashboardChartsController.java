package com.syncup.app.controllers;

import com.syncup.app.Main;
import com.syncup.app.logic.DataStore;
import com.syncup.app.logic.HistorialManager;
import com.syncup.app.model.Cancion;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h2>Controlador del Dashboard de Métricas</h2>
 * Genera visualizaciones gráficas del historial de reproducción global.
 * <p>
 * Proporciona análisis interactivos mediante:
 * </p>
 * <ul>
 *     <li>Gráfico de pastel: Distribución de géneros más reproducidos</li>
 *     <li>Gráfico de barras: Top artistas por número de reproducciones</li>
 *     <li>Lista de top 10 canciones más reproducidas</li>
 *     <li>Filtrado por usuario, género y rango de fechas</li>
 * </ul>
 * <p>
 * <b>Funcionalidades:</b>
 * </p>
 * <ul>
 *     <li>Aplicar y limpiar filtros dinámicamente</li>
 *     <li>Actualizar gráficos en tiempo real según filtros activos</li>
 *     <li>Navegación entre vistas de administrador</li>
 * </ul>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class DashboardChartsController {

    @FXML
    private PieChart chartGeneros;
    @FXML
    private BarChart<String, Number> chartArtistas;
    @FXML
    private ListView<String> listaTopCanciones;

    @FXML
    private ComboBox<String> comboUsuarios;
    @FXML
    private ComboBox<String> comboGeneros;
    @FXML
    private DatePicker dateDesde;
    @FXML
    private DatePicker dateHasta;

    private final HistorialManager historial = DataStore.getInstance().getHistorial();

    // Cached sin filtros
    private List<String[]> historialOriginal;

    @FXML
    private void initialize() {

        historialOriginal = historial.obtenerHistorial();

        cargarUsuarios();
        cargarGeneros();
        refrescarGraficos(historialOriginal);
    }

    // CARGA DE LISTAS

    private void cargarUsuarios() {
        Set<String> usuarios = historialOriginal.stream()
                .map(r -> r[0])
                .collect(Collectors.toSet());

        comboUsuarios.getItems().clear();
        comboUsuarios.getItems().add("Todos");
        comboUsuarios.getItems().addAll(usuarios);
        comboUsuarios.getSelectionModel().selectFirst();
    }

    private void cargarGeneros() {
        Set<String> generos = historialOriginal.stream()
                .map(r -> r[3])
                .collect(Collectors.toSet());

        comboGeneros.getItems().clear();
        comboGeneros.getItems().add("Todos");
        comboGeneros.getItems().addAll(generos);
        comboGeneros.getSelectionModel().selectFirst();
    }

    // FILTROS

    @FXML
    private void handleAplicarFiltros() {
        List<String[]> filtrado = new ArrayList<>(historialOriginal);

        // Filtrar por usuario
        String usuario = comboUsuarios.getValue();
        if (!usuario.equals("Todos")) {
            filtrado = filtrado.stream()
                    .filter(r -> r[0].equals(usuario))
                    .collect(Collectors.toList());
        }

        // Filtrar por género
        String genero = comboGeneros.getValue();
        if (!genero.equals("Todos")) {
            filtrado = filtrado.stream()
                    .filter(r -> r[3].equals(genero))
                    .collect(Collectors.toList());
        }

        // Filtrar por fechas
        LocalDate dDesde = dateDesde.getValue();
        LocalDate dHasta = dateHasta.getValue();

        if (dDesde != null) {
            filtrado = filtrado.stream()
                    .filter(r -> LocalDate.parse(r[1].split(" ")[0]).compareTo(dDesde) >= 0)
                    .collect(Collectors.toList());
        }

        if (dHasta != null) {
            filtrado = filtrado.stream()
                    .filter(r -> LocalDate.parse(r[1].split(" ")[0]).compareTo(dHasta) <= 0)
                    .collect(Collectors.toList());
        }

        refrescarGraficos(filtrado);
    }

    @FXML
    private void handleQuitarFiltros() {
        comboUsuarios.getSelectionModel().selectFirst();
        comboGeneros.getSelectionModel().selectFirst();
        dateDesde.setValue(null);
        dateHasta.setValue(null);

        refrescarGraficos(historialOriginal);
    }

    // GRÁFICOS

    private void refrescarGraficos(List<String[]> registros) {

        cargarPieChart(registros);
        cargarBarChart(registros);
        cargarTopCanciones(registros);
    }

    private void cargarPieChart(List<String[]> registros) {
        chartGeneros.getData().clear();

        Map<String, Long> mapa = registros.stream()
                .collect(Collectors.groupingBy(r -> r[3], Collectors.counting()));

        mapa.forEach((gen, count) -> chartGeneros.getData().add(new PieChart.Data(gen, count)));
    }

    private void cargarBarChart(List<String[]> registros) {
        chartArtistas.getData().clear();

        Map<String, Long> mapa = registros.stream()
                .map(r -> DataStore.getInstance()
                        .getBiblioteca().buscarPorTitulo(r[2]))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Cancion::getArtista, Collectors.counting()));

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Reproducciones por Artista");

        mapa.forEach((artista, count) -> {
            serie.getData().add(new XYChart.Data<>(artista, count));
        });

        chartArtistas.getData().add(serie);
    }

    private void cargarTopCanciones(List<String[]> registros) {
        listaTopCanciones.getItems().clear();

        Map<String, Long> mapa = registros.stream()
                .collect(Collectors.groupingBy(r -> r[2], Collectors.counting()));

        mapa.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .forEach(e -> listaTopCanciones.getItems()
                        .add(e.getKey() + " — " + e.getValue() + " reproducciones"));
    }

    // VOLVER
    @FXML
    private void handleVolver() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/estadisticas_admin.fxml"));
            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(
                    getClass().getResource("/styles/dark-theme.css").toExternalForm());

            Stage stage = (Stage) chartGeneros.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SyncUp - Dashboard Administrativo");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
