package com.syncup.app;

import com.syncup.app.logic.UsuarioManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioManagerTest {

    UsuarioManager manager;

    @BeforeEach
    void setup() {
        manager = new UsuarioManager();
    }

    @Test
    void testRegistrarUsuarioNuevo() {

        // Generar un username único para que nunca choque con el CSV real
        String randomUser = "userTest_" + System.currentTimeMillis();

        boolean registrado = manager.registrarUsuario(
                randomUser,
                "pass123",
                "usuario",
                "Usuario Prueba"
        );

        assertTrue(registrado, "El usuario debería registrarse correctamente");
    }

    @Test
    void testValidarCredenciales() {

        String randomUser = "userLogin_" + System.currentTimeMillis();

        // Primero registramos
        manager.registrarUsuario(randomUser, "claveX", "usuario", "Prueba");

        // Ahora validamos login
        assertTrue(manager.validarCredenciales(randomUser, "claveX"));
    }

    @Test
    void testNoRegistrarDuplicados() {

        String user = "userDuplicado_" + System.currentTimeMillis();

        // Registro la primera vez
        assertTrue(manager.registrarUsuario(user, "abc123", "usuario", "Test Uno"));

        // Segunda vez → debe fallar
        assertFalse(manager.registrarUsuario(user, "abc123", "usuario", "Test Dos"));
    }
}
