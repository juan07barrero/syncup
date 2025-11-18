package com.syncup.app;

import com.syncup.app.logic.FavoritosManager;
import com.syncup.app.model.Cancion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class FavoritosManagerTest {

    FavoritosManager manager;

    @BeforeEach
    void setup() {
        manager = new FavoritosManager();
    }

    @Test
    void testToggleFavorito() {
        Cancion c = new Cancion("Test", "Artista", "Rock");

        assertTrue(manager.toggleFavorito("juan", c)); // agregado
        assertFalse(manager.toggleFavorito("juan", c)); // eliminado
    }
}

