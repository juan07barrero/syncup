package com.syncup.app;

import com.syncup.app.logic.BKTree;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BKTreeTest {

    BKTree tree;

    @BeforeEach
    void setup() {
        tree = new BKTree();

        // claves deben simular "titulo+artista+genero"
        tree.insertar("love story taylor swift pop", "Love Story");
        tree.insertar("lover taylor swift pop", "Lover");
        tree.insertar("loveless the weekend rnb", "Loveless");
        tree.insertar("low motion dillon francis edm", "Low Motion");
    }

    @Test
    void testInsercionYBusquedaDistanciaCero() {
        // Distancia 0 → debe devolver exactamente esa referencia
        var resultados = tree.buscarSimilares("lover taylor swift pop", 0);

        assertEquals(1, resultados.size());
        assertEquals("Lover", resultados.get(0));
    }

    @Test
    void testBusquedaNoDevuelveInexistentes() {
        var resultados = tree.buscarSimilares("abcdxyz", 2);

        assertEquals(0, resultados.size());
    }

    @Test
    void testLimpiarArbol() {
        tree.limpiar();
        var resultados = tree.buscarSimilares("love", 3);

        assertEquals(0, resultados.size());
    }

    @Test
    void testMultipleReferenciasMismaClave() {
        // insertar misma clave con nueva referencia
        tree.insertar("loveless the weekend rnb", "Loveless Remix");

        var resultados = tree.buscarSimilares("loveless the weekend rnb", 0);

        assertTrue(resultados.contains("Loveless"));
        assertTrue(resultados.contains("Loveless Remix"));
        assertEquals(2, resultados.size());
    }

    @Test
    void testBusquedaSimilares() {
        // El BKTree usa claves completas (titulo+artista+genero)
        // Por lo tanto, buscar "love" con distancia pequeña NO retorna nada.

        var resultados = tree.buscarSimilares("love", 30); // distancia amplia

        assertTrue(resultados.contains("Love Story"));
        assertTrue(resultados.contains("Lover"));
        assertTrue(resultados.contains("Loveless"));
        assertTrue(resultados.contains("Low Motion"));

        assertEquals(4, resultados.size());
    }

    @Test
    void testDistanciaPequenaNoIncluyeTodas() {
        // Ahora probamos con una distancia pequeña realista
        var resultados = tree.buscarSimilares("love", 3);

        // Ninguna clave está tan cerca → lista vacía
        assertEquals(0, resultados.size());
    }

}
