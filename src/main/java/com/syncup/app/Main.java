package com.syncup.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * <h2>Main</h2>
 * Clase principal de la aplicación SyncUp.
 * 
 * <p>
 * Extiende {@link javafx.application.Application} para inicializar la interfaz gráfica
 * usando JavaFX. Es el punto de entrada de la aplicación y carga el archivo de interfaz
 * login.fxml con el tema oscuro aplicado.
 * </p>
 * 
 * <h3>Flujo de ejecución:</h3>
 * <ol>
 *     <li>Carga el archivo FXML (login.fxml)</li>
 *     <li>Aplica el tema oscuro (dark-theme.css)</li>
 *     <li>Configura la ventana principal (primaryStage)</li>
 *     <li>Muestra la interfaz al usuario</li>
 * </ol>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class Main extends Application {

    /**
     * Método start de JavaFX.
     * Se ejecuta cuando la aplicación comienza y es responsable de inicializar
     * la interfaz de usuario.
     * 
     * @param primaryStage ventana principal de la aplicación
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("🚀 Iniciando Main.java — cargando login.fxml...");

            // Carga el archivo FXML de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            // Crea la escena
            Scene scene = new Scene(root);

            // 🎨 Aplica el tema oscuro globalmente
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            // Configura la ventana principal
            primaryStage.setTitle("SyncUp - Inicio de Sesión");
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("✅ Interfaz cargada correctamente con tema oscuro.");
        } catch (Exception e) {
            System.err.println("❌ Error al cargar login.fxml:");
            e.printStackTrace();
        }
    }

    /**
     * Método main de la aplicación.
     * Inicia la aplicación JavaFX.
     * 
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        launch(args);
    }
}

