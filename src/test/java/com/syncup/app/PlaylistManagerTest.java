package com.syncup.app;

import com.syncup.app.logic.DataStore;
import com.syncup.app.logic.PlaylistManager;
import com.syncup.app.model.BibliotecaMusical;
import com.syncup.app.model.Cancion;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlaylistManagerTest {

    private PlaylistManager manager;
    private BibliotecaMusical biblioteca;

    @BeforeEach
    void setup() {
        // Usamos DataStore real para mantener coherencia del proyecto
        DataStore ds = DataStore.getInstance();

        manager = ds.getPlaylists();
        biblioteca = ds.getBiblioteca();

        // Limpiar playlists de prueba
        manager.eliminarTodoUsuario("testuser");

        // Crear playlist base
        manager.crearPlaylist("testuser", "MiLista");
    }

    @Test
    void testCrearPlaylist() {
        boolean creada = manager.crearPlaylist("testuser", "NuevaListaTest");
        assertTrue(creada);
    }

    @Test
    void testAgregarCancionPorTitulo() {
        // Obtenemos una canción real del CSV cargado
        List<Cancion> todas = biblioteca.obtenerTodas();
        assertFalse(todas.isEmpty(), "La biblioteca no puede estar vacía en test.");

        String tituloReal = todas.get(0).getTitulo();

        boolean agregado = manager.agregarCancion("testuser", "MiLista", tituloReal);

        assertTrue(agregado, "La canción existente debería agregarse correctamente.");
    }

    @Test
    void testAgregarCancionObjeto() {
        Cancion c = new Cancion("Canción Prueba", "Artista X", "Pop");

        boolean agregado = manager.agregarCancion("testuser", "MiLista", c);

        assertTrue(agregado, "Debería agregar una canción desde objeto Cancion.");
    }

    @Test
    void testEliminarPlaylist() {
        boolean eliminada = manager.eliminarPlaylist("testuser", "MiLista");
        assertTrue(eliminada);
    }
}
