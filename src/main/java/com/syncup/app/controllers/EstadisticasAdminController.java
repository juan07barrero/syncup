package com.syncup.app.controllers;

import com.syncup.app.Main;
import com.syncup.app.logic.DataStore;
import com.syncup.app.logic.HistorialManager;
import com.syncup.app.logic.UsuarioManager;
import com.syncup.app.model.Usuario;
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
 * <h2>Controlador del Panel Administrativo</h2>
 * Panel de control exclusivo para administradores del sistema SyncUp.
 * <p>
 * Proporciona funcionalidades administrativas:
 * </p>
 * <ul>
 *     <li>Visualización de estadísticas globales del sistema</li>
 *     <li>Gestión de usuarios: crear, eliminar, cambiar roles</li>
 *     <li>Manipulación del historial global de reproducción</li>
 *     <li>Exportación de reportes y datos</li>
 *     <li>Acceso al dashboard de métricas avanzadas</li>
 * </ul>
 * <p>
 * <b>Secciones del panel:</b>
 * </p>
 * <ul>
 *     <li><b>Estadísticas Globales</b>: Métricas del sistema completo</li>
 *     <li><b>Gestión de Usuarios</b>: CRUD de cuentas de usuario</li>
 *     <li><b>Mantenimiento</b>: Limpieza y exportación de datos</li>
 * </ul>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class EstadisticasAdminController {

    // ======= TAB ESTADÍSTICAS =======
    @FXML
    private TableView<Metrica> tablaEstadisticasGlobales;
    @FXML
    private TableColumn<Metrica, String> colTituloGlobal;
    @FXML
    private TableColumn<Metrica, String> colValorGlobal;

    // ======= TAB USUARIOS =======
    @FXML
    private TableView<Usuario> tablaUsuarios;
    @FXML
    private TableColumn<Usuario, String> colUsuario;
    @FXML
    private TableColumn<Usuario, String> colNombre;
    @FXML
    private TableColumn<Usuario, String> colRol;

    private final HistorialManager historialManager = DataStore.getInstance().getHistorial();
    private final UsuarioManager usuarioManager = new UsuarioManager();

    private final ObservableList<Metrica> listaMetricas = FXCollections.observableArrayList();
    private final ObservableList<Usuario> listaUsuarios = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // === Configurar columnas de estadísticas ===
        colTituloGlobal.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colValorGlobal.setCellValueFactory(new PropertyValueFactory<>("valor"));
        tablaEstadisticasGlobales.setItems(listaMetricas);

        // === Configurar columnas de usuarios ===
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        tablaUsuarios.setItems(listaUsuarios);

        // Cargar datos iniciales
        cargarMetricasGlobales();
        cargarUsuarios();
    }

    // ==========================
    // MÉTRICAS GLOBALES
    // ==========================

    private void cargarMetricasGlobales() {
        listaMetricas.clear();

        listaMetricas.add(new Metrica("Total de canciones reproducidas",
                String.valueOf(historialManager.obtenerTotalReproducciones())));

        listaMetricas.add(new Metrica("Canción más reproducida",
                historialManager.obtenerCancionMasReproducida()));

        listaMetricas.add(new Metrica("Género más reproducido",
                historialManager.obtenerGeneroMasReproducido()));

        listaMetricas.add(new Metrica("Promedio de reproducciones por día",
                String.format("%.2f", historialManager.obtenerPromedioReproduccionesPorDia())));
    }

    @FXML
    private void handleActualizarGlobal() {
        cargarMetricasGlobales();
        mostrarAlerta("✅ Actualizado", "Las estadísticas globales se han recargado correctamente.");
    }

    @FXML
    private void handleLimpiarHistorial() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar limpieza");
        confirm.setHeaderText("¿Deseas eliminar todo el historial global?");
        confirm.setContentText("Esta acción no se puede deshacer.");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                historialManager.limpiarHistorialGlobal();
                cargarMetricasGlobales();
                mostrarAlerta("🧹 Limpieza completada", "El historial global ha sido eliminado.");
            }
        });
    }

    @FXML
    private void handleExportarHistorial() {
        try {
            historialManager.exportarHistorialPorUsuario();
            mostrarAlerta("📤 Exportación completada",
                    "El historial se exportó correctamente en /resources/exports/");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo exportar el historial global.");
        }
    }

    // GESTIÓN DE USUARIOS

    private void cargarUsuarios() {
        listaUsuarios.clear();
        listaUsuarios.setAll(usuarioManager.obtenerUsuarios());
    }

    @FXML
    private void handleEliminarUsuario() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un usuario para eliminar.");
            return;
        }

        if (seleccionado.getUsername().equalsIgnoreCase("admin")) {
            mostrarAlerta("Error", "No puedes eliminar la cuenta de administrador.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar usuario " + seleccionado.getUsername() + "?");
        confirm.setContentText("Esta acción eliminará su historial asociado.");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                usuarioManager.eliminarUsuario(seleccionado.getUsername());
                historialManager.eliminarHistorialUsuario(seleccionado.getUsername());
                cargarUsuarios();
                mostrarAlerta("✅ Usuario eliminado", "El usuario fue eliminado exitosamente.");
            }
        });
    }

    @FXML
    private void handleCambiarRol() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un usuario para cambiar su rol.");
            return;
        }

        String nuevoRol = seleccionado.getRol().equalsIgnoreCase("admin") ? "usuario" : "admin";
        usuarioManager.cambiarRol(seleccionado.getUsername(), nuevoRol);
        cargarUsuarios();

        mostrarAlerta("🔄 Rol actualizado",
                "El usuario " + seleccionado.getUsername() + " ahora es " + nuevoRol + ".");
    }

    // NAVEGACIÓN

    @FXML
    private void handleVolver() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/usuario.fxml"));
            Scene scene = new Scene(loader.load());

            // APLICAR ESTILO SIEMPRE
            scene.getStylesheets().add(
                    getClass().getResource("/styles/dark-theme.css").toExternalForm());

            Stage stage = (Stage) tablaUsuarios.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SyncUp - Panel de Usuario");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAbrirDashboardCharts() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/dashboard_charts.fxml"));
            Scene scene = new Scene(loader.load());

            // Aplicar tu tema oscuro
            scene.getStylesheets().add(
                    getClass().getResource("/styles/dark-theme.css").toExternalForm());

            Stage stage = (Stage) tablaUsuarios.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SyncUp - Dashboard de Métricas");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el dashboard de métricas.");
        }
    }

    // UTILIDADES

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /** Clase interna simple para representar una métrica (clave/valor) */
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
