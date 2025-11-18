package com.syncup.app;

import com.syncup.app.logic.Trie;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class TrieTest {

    Trie trie;

    @BeforeEach
    void setup() {
        trie = new Trie();
        trie.insertar("love", "Love Story");
        trie.insertar("lover", "Lover");
        trie.insertar("low", "Low Motion");
    }

    @Test
    void testBuscarPorPrefijo() {
        var resultados = trie.buscarPorPrefijo("lo");

        assertEquals(3, resultados.size());
        assertTrue(resultados.contains("Love Story"));
        assertTrue(resultados.contains("Lover"));
        assertTrue(resultados.contains("Low Motion"));
    }

    @Test
    void testPrefijoInexistente() {
        var resultados = trie.buscarPorPrefijo("xyz");
        assertEquals(0, resultados.size());
    }

    @Test
    void testLimpiarTrie() {
        trie.clear();
        var resultados = trie.buscarPorPrefijo("lo");
        assertEquals(0, resultados.size());
    }
}

