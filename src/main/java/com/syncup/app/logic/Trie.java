package com.syncup.app.logic;

import java.util.*;

/**
 * <h2>Trie (Árbol de Prefijos)</h2>
 * Estructura de datos especializada para búsquedas rápidas por prefijo.
 * 
 * <p>
 * Implementa un árbol de prefijos que permite búsqueda y autocompletado eficiente
 * de canciones, artistas y géneros. Cada nodo almacena referencias a los resultados
 * encontrados en su subárbol.
 * </p>
 * 
 * <h3>Uso en el sistema:</h3>
 * <ul>
 *     <li>Autocompletado en búsquedas de canciones</li>
 *     <li>Búsqueda rápida de artistas y géneros</li>
 *     <li>Sugerencias mientras el usuario escribe</li>
 * </ul>
 * 
 * <h3>Complejidad:</h3>
 * <ul>
 *     <li>Inserción: O(m) donde m es la longitud de la palabra</li>
 *     <li>Búsqueda: O(m) donde m es la longitud del prefijo</li>
 * </ul>
 * 
 * <h3>Ejemplo:</h3>
 * <pre>
 *   Trie trie = new Trie();
 *   trie.insertar("Bohemian Rhapsody", "Bohemian Rhapsody");
 *   trie.insertar("Blinding Lights", "Blinding Lights");
 *   
 *   List&lt;String&gt; sugerencias = trie.buscarPorPrefijo("Boh");
 *   // Retorna: ["Bohemian Rhapsody"]
 * </pre>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class Trie {

    /**
     * Nodo interno del Trie.
     * Cada nodo representa un carácter en el árbol.
     */
    private static class Nodo {
        /** Hijos del nodo (mapa carácter → nodo) */
        Map<Character, Nodo> hijos = new HashMap<>();
        /** Indica si este nodo es el final de una palabra completa */
        boolean esFinDePalabra = false;
        /** Almacena todas las referencias (títulos) que pasan por este nodo */
        List<String> resultados = new ArrayList<>();
    }

    /** Raíz del Trie (punto de partida para todas las búsquedas) */
    private Nodo raiz = new Nodo();

    /**
     * Inserta una palabra en el Trie asociada con una referencia.
     * La palabra se divide en caracteres y cada carácter crea o reutiliza un nodo.
     * La referencia se agrega a todos los nodos del camino.
     * 
     * @param palabra palabra a insertar (se convierte a minúsculas)
     * @param referencia identificador asociado (ej: título de canción)
     */
    public void insertar(String palabra, String referencia) {
        if (palabra == null || palabra.isEmpty())
            return;

        palabra = palabra.toLowerCase();
        Nodo actual = raiz;

        for (char c : palabra.toCharArray()) {
            actual = actual.hijos.computeIfAbsent(c, k -> new Nodo());

            // Se agrega la referencia (título) a todos los nodos del camino
            if (!actual.resultados.contains(referencia)) {
                actual.resultados.add(referencia);
            }
        }

        actual.esFinDePalabra = true;
    }

    /**
     * Busca todas las palabras que comienzan con un prefijo dado.
     * 
     * <p>
     * Navega por el árbol siguiendo los caracteres del prefijo.
     * Una vez alcanzado el nodo del prefijo, retorna todos los resultados
     * almacenados en ese nodo (que incluyen palabras que comienzan con ese prefijo).
     * </p>
     * 
     * @param prefijo cadena de búsqueda (se convierte a minúsculas)
     * @return lista de referencias que coinciden con el prefijo, lista vacía si no hay coincidencias
     */
    public List<String> buscarPorPrefijo(String prefijo) {
        if (prefijo == null || prefijo.isEmpty())
            return List.of();

        prefijo = prefijo.toLowerCase();
        Nodo actual = raiz;

        for (char c : prefijo.toCharArray()) {
            if (!actual.hijos.containsKey(c))
                return List.of(); // No existe ese prefijo
            actual = actual.hijos.get(c);
        }

        // Las coincidencias ya están prealmacenadas en este nodo
        return new ArrayList<>(actual.resultados);
    }

    /**
     * Limpia completamente el Trie, eliminando todos los nodos y datos almacenados.
     * Se utiliza cuando es necesario reconstruir el Trie desde cero.
     */
    public void clear() {
        raiz = new Nodo();
    }
}

