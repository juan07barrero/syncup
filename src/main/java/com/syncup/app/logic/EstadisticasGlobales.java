package com.syncup.app.logic;

import java.util.*;

/**
 * <h2>EstadisticasGlobales</h2>
 * Calcula estadísticas globales del sistema basadas en el historial de reproducciones.
 * <p>
 * Proporciona métodos para obtener información agregada sobre:
 * </p>
 * <ul>
 *     <li>Canciones más reproducidas</li>
 *     <li>Géneros más escuchados</li>
 *     <li>Conteos por canción y género</li>
 *     <li>Porcentajes de distribución</li>
 * </ul>
 * <p>
 * <b>Formato de datos del HistorialManager:</b>
 * </p>
 * <pre>
 *     registro[0] = usuario
 *     registro[1] = fecha y hora
 *     registro[2] = título de la canción
 *     registro[3] = género
 * </pre>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class EstadisticasGlobales {

    /** Referencia al manejador del historial */
    private final HistorialManager historialManager;

    /**
     * Constructor.
     * 
     * @param historialManager manejador del historial de reproducciones
     */
    public EstadisticasGlobales(HistorialManager historialManager) {
        this.historialManager = historialManager;
    }

    /**
     * Obtiene un conteo de reproducciones agrupadas por género.
     * 
     * @return mapa género → cantidad de reproducciones
     */
    public Map<String, Integer> obtenerReproduccionesPorGenero() {
        Map<String, Integer> conteo = new HashMap<>();

        for (String[] r : historialManager.obtenerHistorial()) {
            if (r.length < 4) continue;
            String genero = r[3] != null ? r[3].trim() : "Desconocido";

            conteo.put(genero, conteo.getOrDefault(genero, 0) + 1);
        }
        return conteo;
    }

    /**
     * Obtiene un conteo de reproducciones agrupadas por canción.
     * 
     * @return mapa título de canción → cantidad de reproducciones
     */
    public Map<String, Integer> obtenerReproduccionesPorCancion() {
        Map<String, Integer> conteo = new HashMap<>();

        for (String[] r : historialManager.obtenerHistorial()) {
            if (r.length < 3) continue;
            String titulo = r[2] != null ? r[2].trim() : "Sin título";

            conteo.put(titulo, conteo.getOrDefault(titulo, 0) + 1);
        }
        return conteo;
    }

    /**
     * Obtiene el top N de canciones más escuchadas ordenadas de mayor a menor.
     * 
     * @param limite número máximo de resultados (0 para sin límite)
     * @return lista ordenada de pares (canción, cantidad)
     */
    public List<Map.Entry<String, Integer>> obtenerTopCanciones(int limite) {
        Map<String, Integer> conteo = obtenerReproduccionesPorCancion();
        List<Map.Entry<String, Integer>> lista = new ArrayList<>(conteo.entrySet());

        lista.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        if (limite > 0 && lista.size() > limite) {
            return lista.subList(0, limite);
        }
        return lista;
    }

    /**
     * Obtiene el top N de géneros más escuchados ordenados de mayor a menor.
     * 
     * @param limite número máximo de resultados (0 para sin límite)
     * @return lista ordenada de pares (género, cantidad)
     */
    public List<Map.Entry<String, Integer>> obtenerTopGeneros(int limite) {
        Map<String, Integer> conteo = obtenerReproduccionesPorGenero();
        List<Map.Entry<String, Integer>> lista = new ArrayList<>(conteo.entrySet());

        lista.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        if (limite > 0 && lista.size() > limite) {
            return lista.subList(0, limite);
        }
        return lista;
    }

    /**
     * Obtiene el número total de canciones distintas reproducidas.
     * 
     * @return cantidad de canciones únicas en el historial
     */
    public int obtenerCantidadCancionesDistintas() {
        Set<String> canciones = new HashSet<>();
        for (String[] r : historialManager.obtenerHistorial()) {
            if (r.length < 3) continue;
            String titulo = r[2] != null ? r[2].trim() : "Sin título";
            canciones.add(titulo);
        }
        return canciones.size();
    }

    /**
     * Obtiene el número total de géneros distintos reproducidos.
     * 
     * @return cantidad de géneros únicos en el historial
     */
    public int obtenerCantidadGenerosDistintos() {
        Set<String> generos = new HashSet<>();
        for (String[] r : historialManager.obtenerHistorial()) {
            if (r.length < 4) continue;
            String genero = r[3] != null ? r[3].trim() : "Desconocido";
            generos.add(genero);
        }
        return generos.size();
    }

    /**
     * Calcula el porcentaje de reproducciones que representa cada género.
     * Los porcentajes suman aproximadamente 100%.
     * 
     * @return mapa género → porcentaje (0-100)
     */
    public Map<String, Double> obtenerPorcentajePorGenero() {
        Map<String, Integer> conteo = obtenerReproduccionesPorGenero();
        Map<String, Double> porcentajes = new HashMap<>();

        int total = historialManager.obtenerTotalReproducciones();
        if (total == 0) return porcentajes;

        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            double pct = (entry.getValue() * 100.0) / total;
            porcentajes.put(entry.getKey(), pct);
        }
        return porcentajes;
    }
}

