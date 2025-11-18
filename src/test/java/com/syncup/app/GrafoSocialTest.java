package com.syncup.app;

import com.syncup.app.logic.GrafoSocial;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class GrafoSocialTest {

    GrafoSocial grafo;

    @BeforeEach
    void setup() {
        grafo = new GrafoSocial();
    }

    @Test
    void testSeguirYDejar() {
        assertTrue(grafo.seguir("juan", "maria"));
        assertTrue(grafo.dejarDeSeguir("juan", "maria"));
    }
}
