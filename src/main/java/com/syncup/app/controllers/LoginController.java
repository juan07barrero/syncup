package com.syncup.app.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.syncup.app.Main;
import com.syncup.app.logic.DataStore;
import java.io.IOException;

/**
 * <h2>Controlador de Inicio de Sesión</h2>
 * Gestiona la autenticación de usuarios en el sistema SyncUp.
 * <p>
 * Proporciona funcionalidades para:
 * </p>
 * <ul>
 *     <li>Validar credenciales de usuarios existentes</li>
 *     <li>Registrar nuevas cuentas de usuario</li>
 *     <li>Navegar al panel de usuario tras login exitoso</li>
 * </ul>
 * <p>
 * <b>Campos FXML:</b>
 * </p>
 * <ul>
 *     <li><b>txtUsername</b>: Campo para ingresar nombre de usuario</li>
 *     <li><b>txtPassword</b>: Campo seguro para ingresar contraseña</li>
 *     <li><b>lblMensaje</b>: Etiqueta para mostrar mensajes de estado</li>
 * </ul>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMensaje;

    // Acción al presionar el botón de inicio de sesión
    @FXML
    private void handleLogin() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Por favor ingresa usuario y contraseña.");
            return;
        }

        DataStore data = DataStore.getInstance();

        if (data.validarUsuario(user, pass)) {

            // Registrar usuario activo
            data.setUsuarioActivo(user);
            data.getFavoritos().cargarFavoritos(user);
            data.getPlaylists().cargarPlaylists(user);

            try {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/usuario.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

                Stage stage = (Stage) txtUsername.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("SyncUp - Panel de Usuario");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo abrir el panel de usuario.");
            }

        } else {
            mostrarAlerta("Error de inicio de sesión", "Credenciales incorrectas. Intenta nuevamente.");
        }
    }

    @FXML
    private void handleRegistrar(ActionEvent event) {
        // Pedir el nombre completo
        TextInputDialog nombreDialog = new TextInputDialog();
        nombreDialog.setTitle("Registro de usuario");
        nombreDialog.setHeaderText("Crear una nueva cuenta");
        nombreDialog.setContentText("Nombre completo:");
        String nombre = nombreDialog.showAndWait().orElse(null);

        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarAlerta("Registro cancelado", "Debes ingresar tu nombre para continuar.");
            return;
        }

        // Pedir la contraseña
        TextInputDialog passDialog = new TextInputDialog();
        passDialog.setTitle("Registro de usuario");
        passDialog.setHeaderText("Crear una nueva cuenta");
        passDialog.setContentText("Contraseña:");
        String pass = passDialog.showAndWait().orElse(null);

        if (pass == null || pass.trim().isEmpty()) {
            mostrarAlerta("Registro cancelado", "Debes ingresar una contraseña para continuar.");
            return;
        }

        // Obtener usuario desde el campo principal
        String user = txtUsername.getText() == null ? "" : txtUsername.getText().trim();

        if (user.isEmpty()) {
            mostrarAlerta("Error", "Debes ingresar un nombre de usuario antes de registrar.");
            return;
        }

        // Registrar en DataStore
        boolean ok = DataStore.getInstance().registrarUsuario(user, pass, "usuario", nombre);

        if (ok) {
            mostrarAlerta("Éxito", "Usuario registrado correctamente. Ahora puedes iniciar sesión.");
            txtUsername.clear();
            txtPassword.clear();
            if (lblMensaje != null)
                lblMensaje.setText("");
        } else {
            mostrarAlerta("Error", "El usuario ya existe o hubo un problema al registrar.");
        }
    }

    // Método auxiliar para mostrar alertas
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
