package com.syncup.app.controllers;

import com.syncup.app.Main;
import com.syncup.app.logic.DataStore;
import com.syncup.app.logic.HistorialManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controlador de la vista de estadísticas globales (solo para admin).
 * Muestra métricas agrupadas por usuario, canción y género.
 */
public class EstadisticasGlobalesController {

    @FXML
    private TableView<Metrica> tablaEstadisticas;

    @FXML
    private TableColumn<Metrica, String> colTitulo;

    @FXML
    private TableColumn<Metrica, String> colValor;

    private final ObservableList<Metrica> listaMetricas = FXCollections.observableArrayList();

    private final DataStore dataStore = DataStore.getInstance();

    @FXML
    public void initialize() {
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        tablaEstadisticas.setItems(listaMetricas);

        cargarMetricasGlobales();
    }

    /** Carga estadísticas agrupadas (solo para admin) */
    private void cargarMetricasGlobales() {
        listaMetricas.clear();
        HistorialManager manager = dataStore.getHistorial();

        // ✅ Estadísticas generales
        listaMetricas.add(new Metrica("👥 Total de reproducciones globales",
                String.valueOf(manager.obtenerTotalReproducciones())));
        listaMetricas.add(new Metrica("🎵 Canción más reproducida (global)",
                manager.obtenerCancionMasReproducida()));
        listaMetricas.add(new Metrica("🎧 Género más reproducido (global)",
                manager.obtenerGeneroMasReproducido()));
        listaMetricas.add(new Metrica("📈 Promedio de reproducciones por día",
                String.format("%.2f", manager.obtenerPromedioReproduccionesPorDia())));

        listaMetricas.add(new Metrica("────────────", "────────────"));

        // ✅ Reproducciones por usuario
        Map<String, List<String[]>> agrupado = manager.obtenerHistorialAgrupadoPorUsuario();
        listaMetricas.add(new Metrica("👤 Usuarios con actividad", String.valueOf(agrupado.size())));

        for (Map.Entry<String, List<String[]>> entry : agrupado.entrySet()) {
            String usuario = entry.getKey();
            int total = entry.getValue().size();
            listaMetricas.add(new Metrica(" - " + usuario, total + " reproducciones"));
        }

        listaMetricas.add(new Metrica("────────────", "────────────"));

        // ✅ Canciones más escuchadas (top 3)
        Map<String, Long> conteoCanciones = manager.conteoPorCancion();
        listaMetricas.add(new Metrica("🎶 Top 3 Canciones más reproducidas", ""));
        conteoCanciones.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(3)
                .forEach(e -> listaMetricas.add(
                        new Metrica(" - " + e.getKey(), e.getValue() + " veces")));

        listaMetricas.add(new Metrica("────────────", "────────────"));

        // ✅ Géneros más escuchados (top 3)
        Map<String, Long> conteoGeneros = manager.conteoPorGenero();
        listaMetricas.add(new Metrica("🎼 Top 3 Géneros más reproducidos", ""));
        conteoGeneros.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(3)
                .forEach(e -> listaMetricas.add(
                        new Metrica(" - " + e.getKey(), e.getValue() + " veces")));
    }

    @FXML
    private void handleActualizar() {
        cargarMetricasGlobales();
        mostrarAlerta("Estadísticas actualizadas", "Los datos globales se han recalculado correctamente.");
    }

    @FXML
    private void handleVolver() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/usuario.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) tablaEstadisticas.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SyncUp - Panel de Usuario");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo volver al panel de usuario.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // ===================================================
    // Clase interna Metrica (igual que en EstadisticasController)
    // ===================================================
    public static class Metrica {
        private final String titulo;
        private final String valor;

        public Metrica(String titulo, String valor) {
            this.titulo = titulo;
            this.valor = valor;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getValor() {
            return valor;
        }
    }
}
