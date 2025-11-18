package com.syncup.app.logic;

import java.util.*;

/**
 * <h2>Árbol BK (Burkhard-Keller)</h2>
 * Estructura de datos especializada para búsquedas de similitud de cadenas.
 * 
 * <p>
 * Implementa la métrica de distancia de Levenshtein para encontrar palabras o
 * frases "parecidas" dentro de un cierto rango de distancia. Es utilizado en
 * el sistema para buscar canciones por título, artista o género con tolerancia
 * a pequeños errores tipográficos.
 * </p>
 * 
 * <h3>Características:</h3>
 * <ul>
 *     <li>Búsqueda aproximada de cadenas</li>
 *     <li>Distancia de Levenshtein como métrica</li>
 *     <li>Estructura de árbol balanceado</li>
 *     <li>Eficiente para búsquedas fuzzy</li>
 * </ul>
 * 
 * <h3>Ejemplo de uso:</h3>
 * <pre>
 *   BKTree tree = new BKTree();
 *   tree.insertar("bohemian rhapsody queen rock", "Bohemian Rhapsody");
 *   
 *   // Busca con tolerancia de distancia 2
 *   List&lt;String&gt; resultados = tree.buscarSimilares("bohemian rapsody", 2);
 * </pre>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class BKTree {

    /**
     * Nodo interno del árbol BK.
     * Cada nodo contiene una clave (cadena base), referencias a canciones
     * y conexiones a nodos hijos indexados por distancia.
     */
    private static class Nodo {
        /** Clave de búsqueda (combinación de título+artista+género) */
        String clave;
        /** Referencias a títulos de canciones con esta clave */
        List<String> referencias = new ArrayList<>();
        /** Hijos del nodo, indexados por distancia Levenshtein */
        Map<Integer, Nodo> hijos = new HashMap<>();

        /**
         * Crea un nodo del árbol.
         * @param clave cadena base del nodo
         * @param referencia referencia inicial (título de canción)
         */
        Nodo(String clave, String referencia) {
            this.clave = clave;
            this.referencias.add(referencia);
        }
    }

    /** Raíz del árbol BK */
    private Nodo raiz = null;

    // ========================
    // INSERCIÓN
    // ========================

    /**
     * Inserta una clave y su referencia en el árbol.
     * Si la clave ya existe, agrega la referencia al nodo existente.
     * 
     * @param clave cadena a indexar (ej: "título+artista+género")
     * @param referencia identificador o referencia asociado (ej: título de canción)
     */
    public void insertar(String clave, String referencia) {
        if (clave == null || clave.isEmpty() || referencia == null)
            return;

        clave = clave.toLowerCase();

        if (raiz == null) {
            raiz = new Nodo(clave, referencia);
            return;
        }

        Nodo actual = raiz;
        int dist;

        while (true) {
            dist = distanciaLevenshtein(clave, actual.clave);

            if (dist == 0) {
                // misma clave: solo agregamos la referencia si no está
                if (!actual.referencias.contains(referencia)) {
                    actual.referencias.add(referencia);
                }
                return;
            }

            Nodo hijo = actual.hijos.get(dist);
            if (hijo != null) {
                actual = hijo;
            } else {
                actual.hijos.put(dist, new Nodo(clave, referencia));
                return;
            }
        }
    }

    // ========================
    // BÚSQUEDA DE SIMILARES
    // ========================

    /**
     * Busca todas las cadenas similares a la consulta dentro de una distancia máxima.
     * 
     * @param consulta cadena a buscar
     * @param maxDistancia distancia máxima permitida (Levenshtein)
     * @return lista de referencias encontradas
     */
    public List<String> buscarSimilares(String consulta, int maxDistancia) {
        List<String> resultados = new ArrayList<>();
        if (raiz == null || consulta == null || consulta.isEmpty())
            return resultados;

        consulta = consulta.toLowerCase();
        buscarRec(raiz, consulta, maxDistancia, resultados);
        return resultados;
    }

    /**
     * Búsqueda recursiva en el árbol BK.
     * 
     * @param nodo nodo actual
     * @param consulta cadena a buscar
     * @param maxDist distancia máxima permitida
     * @param resultados lista acumulativa de resultados
     */
    private void buscarRec(Nodo nodo, String consulta, int maxDist, List<String> resultados) {
        int dist = distanciaLevenshtein(consulta, nodo.clave);

        if (dist <= maxDist) {
            resultados.addAll(nodo.referencias);
        }

        int from = dist - maxDist;
        int to = dist + maxDist;

        for (Map.Entry<Integer, Nodo> entry : nodo.hijos.entrySet()) {
            int d = entry.getKey();
            if (d >= from && d <= to) {
                buscarRec(entry.getValue(), consulta, maxDist, resultados);
            }
        }
    }

    // ========================
    // DISTANCIA DE LEVENSHTEIN
    // ========================

    /**
     * Calcula la distancia de Levenshtein entre dos cadenas.
     * 
     * <p>
     * La distancia de Levenshtein es el número mínimo de ediciones de un carácter
     * (inserción, eliminación o sustitución) necesarias para transformar una cadena en otra.
     * </p>
     * 
     * @param a primera cadena
     * @param b segunda cadena
     * @return distancia de Levenshtein
     */
    private int distanciaLevenshtein(String a, String b) {
        int n = a.length();
        int m = b.length();

        if (n == 0) return m;
        if (m == 0) return n;

        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                char cb = b.charAt(j - 1);
                int costo = (ca == cb) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1,      // eliminación
                                 dp[i][j - 1] + 1),     // inserción
                        dp[i - 1][j - 1] + costo        // sustitución
                );
            }
        }
        return dp[n][m];
    }

    /**
     * Limpia el árbol BK eliminando todos los nodos.
     */
    public void limpiar() {
        raiz = null;
    }
}
