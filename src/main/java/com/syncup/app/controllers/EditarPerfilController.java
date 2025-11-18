package com.syncup.app.controllers;

import com.syncup.app.logic.UsuarioManager;
import com.syncup.app.model.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * <h2>Controlador de Edición de Perfil</h2>
 * Permite a los usuarios actualizar su información personal.
 * <p>
 * Funcionalidades:
 * </p>
 * <ul>
 *     <li>Modificar nombre de perfil</li>
 *     <li>Cambiar contraseña con validación</li>
 *     <li>Persistencia de cambios en base de datos de usuarios</li>
 * </ul>
 * <p>
 * <b>Campos FXML:</b>
 * </p>
 * <ul>
 *     <li><b>txtNombre</b>: Campo para nombre de usuario</li>
 *     <li><b>txtPassword</b>: Primera contraseña</li>
 *     <li><b>txtPassword2</b>: Confirmación de contraseña</li>
 * </ul>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class EditarPerfilController {

    @FXML private TextField txtNombre;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtPassword2;

    private Usuario usuarioActual;
    private UsuarioManager usuarioManager;

    public void inicializar(Usuario usuario, UsuarioManager manager) {
        this.usuarioActual = usuario;
        this.usuarioManager = manager;

        txtNombre.setText(usuario.getNombre());
    }

    @FXML
    private void guardarCambios() {
        String nuevoNombre = txtNombre.getText().trim();
        String pass1 = txtPassword.getText();
        String pass2 = txtPassword2.getText();

        if (nuevoNombre.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
            show("Todos los campos son obligatorios.");
            return;
        }

        if (!pass1.equals(pass2)) {
            show("Las contraseñas no coinciden.");
            return;
        }

        usuarioActual.setNombre(nuevoNombre);
        usuarioActual.setPassword(pass1);

        usuarioManager.actualizarPerfil(usuarioActual);

        show("Perfil actualizado correctamente.");
    }

    private void show(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

