package com.syncup.app.logic;

import com.syncup.app.model.BibliotecaMusical;
import com.syncup.app.model.Cancion;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h2>RecomendadorMusical</h2>
 * Sistema de recomendación musical basado en el historial de reproducciones del usuario.
 * 
 * <p>
 * Analiza los géneros escuchados por cada usuario para generar recomendaciones personalizadas.
 * Si el usuario no tiene historial, muestra las canciones más populares globalmente.
 * </p>
 * 
 * <h3>Algoritmo:</h3>
 * <ol>
 *     <li>Obtener historial del usuario</li>
 *     <li>Contar qué géneros ha reproducido más</li>
 *     <li>Buscar canciones del género favorito</li>
 *     <li>Si faltan canciones, rellenar con populares</li>
 * </ol>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 * @see Recomendador para otra estrategia basada en favoritos
 */
public class RecomendadorMusical {

    /** Manejador del historial global de reproducciones */
    private final HistorialManager historialManager;
    /** Biblioteca musical global para buscar canciones */
    private final BibliotecaMusical biblioteca;

    /**
     * Constructor del recomendador musical.
     * 
     * @param historialManager manejador del historial
     * @param biblioteca biblioteca musical global
     */
    public RecomendadorMusical(HistorialManager historialManager, BibliotecaMusical biblioteca) {
        this.historialManager = historialManager;
        this.biblioteca = biblioteca;
    }

    /**
     * Genera una lista de canciones recomendadas para un usuario específico.
     * 
     * <p>
     * Analiza el historial del usuario para determinar sus géneros favoritos
     * y recomienda canciones de esos géneros. Si el usuario no tiene historial,
     * muestra recomendaciones generales basadas en lo más popular.
     * </p>
     * 
     * @param usuario nombre de usuario
     * @return lista de canciones recomendadas (máximo 5)
     */
    public List<Cancion> generarRecomendaciones(String usuario) {
        if (usuario == null || usuario.isEmpty()) {
            return obtenerRecomendacionesGenerales();
        }

        List<String[]> historialUsuario = historialManager.obtenerHistorialUsuario(usuario);
        if (historialUsuario.isEmpty()) {
            return obtenerRecomendacionesGenerales();
        }

        // Contar géneros más escuchados del usuario
        Map<String, Long> conteoGeneros = historialUsuario.stream()
                .collect(Collectors.groupingBy(r -> r[3], Collectors.counting()));

        // Obtener el género más escuchado
        String generoFav = conteoGeneros.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (generoFav == null) return obtenerRecomendacionesGenerales();

        // Buscar canciones del género favorito
        List<Cancion> todas = biblioteca.obtenerTodas();
        List<Cancion> recomendadas = todas.stream()
                .filter(c -> c.getGenero().equalsIgnoreCase(generoFav))
                .limit(5)
                .collect(Collectors.toList());

        // Si hay menos de 5, rellenar con otras aleatorias
        if (recomendadas.size() < 5) {
            List<Cancion> faltantes = obtenerRecomendacionesGenerales().stream()
                    .filter(c -> !recomendadas.contains(c))
                    .limit(5 - recomendadas.size())
                    .collect(Collectors.toList());
            recomendadas.addAll(faltantes);
        }

        return recomendadas;
    }

    /**
     * Obtiene una lista general de canciones populares globalmente.
     * Se usa cuando el usuario no tiene historial o como relleno.
     * 
     * <p>
     * Si hay historial global, retorna el top 5 más reproducido.
     * Si no hay historial, retorna 5 canciones aleatorias de la biblioteca.
     * </p>
     * 
     * @return lista de canciones populares (máximo 5)
     */
    public List<Cancion> obtenerRecomendacionesGenerales() {
        Map<String, Long> conteo = historialManager.conteoPorCancion();

        // Si no hay historial global, devolver canciones aleatorias
        if (conteo.isEmpty()) {
            List<Cancion> todas = biblioteca.obtenerTodas();
            Collections.shuffle(todas);
            return todas.stream().limit(5).collect(Collectors.toList());
        }

        // Devolver top 5 canciones más reproducidas globalmente
        List<String> topTitulos = conteo.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        // Buscar las canciones correspondientes en la biblioteca
        return biblioteca.obtenerTodas().stream()
                .filter(c -> topTitulos.contains(c.getTitulo()))
                .collect(Collectors.toList());
    }
}
