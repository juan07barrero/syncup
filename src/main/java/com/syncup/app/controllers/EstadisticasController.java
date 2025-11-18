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

/**
 * Controlador de la vista de estadísticas del usuario.
 * Muestra las métricas individuales o globales según el rol.
 */
public class EstadisticasController {

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

        cargarMetricas();
    }

    /**
     * Carga las métricas según el tipo de usuario (normal o admin)
     */
    private void cargarMetricas() {
        listaMetricas.clear();

        HistorialManager manager = dataStore.getHistorial();
        String rol = dataStore.getRolUsuarioActivo();
        String usuario = dataStore.getUsuarioActivo() != null ? dataStore.getUsuarioActivo() : "Invitado";

        if (rol.equalsIgnoreCase("admin")) {
            // 🔹 Estadísticas globales
            listaMetricas.add(new Metrica("👥 Tipo de vista", "Administrador (global)"));
            listaMetricas.add(new Metrica("Total de canciones reproducidas (global)",
                    String.valueOf(manager.obtenerTotalReproducciones())));
            listaMetricas.add(new Metrica("Canción más reproducida (global)",
                    manager.obtenerCancionMasReproducida()));
            listaMetricas.add(new Metrica("Género más reproducido (global)",
                    manager.obtenerGeneroMasReproducido()));
            listaMetricas.add(new Metrica("Promedio de canciones por día (global)",
                    String.format("%.2f", manager.obtenerPromedioReproduccionesPorDia())));
        } else {
            // 🔹 Estadísticas del usuario actual
            listaMetricas.add(new Metrica("👤 Usuario", usuario));
            listaMetricas.add(new Metrica("Rol", rol));

            int totalUsuario = manager.obtenerHistorialUsuario(usuario).size();
            listaMetricas.add(new Metrica("Total de canciones reproducidas",
                    String.valueOf(totalUsuario)));

            listaMetricas.add(new Metrica("Canción más reproducida",
                    manager.obtenerCancionMasReproducidaUsuario(usuario)));

            listaMetricas.add(new Metrica("Género más reproducido",
                    manager.obtenerGeneroMasReproducidoUsuario(usuario)));
        }
    }

    @FXML
    private void handleActualizar() {
        cargarMetricas();
        mostrarAlerta("Estadísticas actualizadas", "Se han recalculado los datos correctamente.");
    }

    @FXML
    private void handleVolver() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/usuario.fxml"));
            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(
                    getClass().getResource("/styles/dark-theme.css").toExternalForm());

            Stage stage = (Stage) tablaEstadisticas.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SyncUp - Panel de Usuario");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Clase interna para representar una métrica

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
