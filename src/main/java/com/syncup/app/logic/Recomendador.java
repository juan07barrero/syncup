package com.syncup.app.logic;

import com.syncup.app.model.BibliotecaMusical;
import com.syncup.app.model.Cancion;

import java.util.*;

/**
 * <h2>Recomendador</h2>
 * Motor de recomendación de canciones basado en los favoritos del usuario.
 * 
 * <p>
 * Analiza el historial de favoritos de un usuario para generar una "radio"
 * personalizada con canciones del mismo género y artista.
 * </p>
 * 
 * <h3>Algoritmo:</h3>
 * <ol>
 *     <li>Extrae los géneros y artistas favoritos del usuario</li>
 *     <li>Asigna puntuaciones: género (+3), artista (+2)</li>
 *     <li>Ordena por puntuación y retorna los mejores</li>
 *     <li>Si no hay datos, retorna canciones generales</li>
 * </ol>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class Recomendador {

    /** Biblioteca musical global para buscar canciones */
    private final BibliotecaMusical biblioteca;
    /** Manager de favoritos para acceder al historial del usuario */
    private final FavoritosManager favoritosManager;

    /**
     * Constructor del recomendador.
     * 
     * @param biblioteca referencia a la biblioteca musical global
     * @param favoritosManager referencia al manager de favoritos
     */
    public Recomendador(BibliotecaMusical biblioteca, FavoritosManager favoritosManager) {
        this.biblioteca = biblioteca;
        this.favoritosManager = favoritosManager;
    }

    /**
     * Genera una lista de canciones recomendadas para un usuario.
     * 
     * <p>
     * El algoritmo funciona analizando los géneros y artistas de las canciones
     * favoritas del usuario y generando puntuaciones para las demás canciones.
     * </p>
     * 
     * @param usuario nombre de usuario
     * @param max número máximo de recomendaciones a retornar
     * @return lista de canciones recomendadas (máximo max canciones)
     */
    public List<Cancion> recomendarParaUsuario(String usuario, int max) {
        List<Cancion> todas = biblioteca.obtenerTodas();
        if (todas == null || todas.isEmpty()) {
            return List.of();
        }

        List<Cancion> favoritos = favoritosManager.obtenerFavoritos(usuario);
        Set<String> titulosFav = new HashSet<>();
        Set<String> generosFav = new HashSet<>();
        Set<String> artistasFav = new HashSet<>();

        if (favoritos != null) {
            for (Cancion c : favoritos) {
                if (c == null) continue;
                if (c.getTitulo() != null)
                    titulosFav.add(c.getTitulo().toLowerCase());
                if (c.getGenero() != null)
                    generosFav.add(c.getGenero().toLowerCase());
                if (c.getArtista() != null)
                    artistasFav.add(c.getArtista().toLowerCase());
            }
        }

        // Si no tiene favoritos, devolvemos simplemente las primeras N canciones de la biblioteca
        if (generosFav.isEmpty() && artistasFav.isEmpty()) {
            int limite = Math.min(max, todas.size());
            return new ArrayList<>(todas.subList(0, limite));
        }

        // Clase interna para puntuar canciones
        class Scored {
            /** Canción candidata */
            Cancion cancion;
            /** Puntuación calculada */
            int score;

            /**
             * Constructor.
             * @param c canción
             * @param s puntuación
             */
            Scored(Cancion c, int s) {
                this.cancion = c;
                this.score = s;
            }
        }

        List<Scored> candidatos = new ArrayList<>();

        for (Cancion c : todas) {
            if (c == null) continue;
            String tituloLower = c.getTitulo() != null ? c.getTitulo().toLowerCase() : "";

            // No recomendamos canciones que ya son favoritas
            if (titulosFav.contains(tituloLower)) continue;

            int score = 0;

            if (c.getGenero() != null && generosFav.contains(c.getGenero().toLowerCase())) {
                score += 3; // peso fuerte por género
            }
            if (c.getArtista() != null && artistasFav.contains(c.getArtista().toLowerCase())) {
                score += 2; // peso por artista
            }

            if (score > 0) {
                candidatos.add(new Scored(c, score));
            }
        }

        // Si no hay candidatos (muy pocos datos), devolvemos canciones no favoritas
        if (candidatos.isEmpty()) {
            List<Cancion> fallback = new ArrayList<>();
            for (Cancion c : todas) {
                if (c == null) continue;
                String tituloLower = c.getTitulo() != null ? c.getTitulo().toLowerCase() : "";
                if (!titulosFav.contains(tituloLower)) {
                    fallback.add(c);
                }
                if (fallback.size() >= max) break;
            }
            return fallback;
        }

        // Aleatorizamos un poco y luego ordenamos por score descendente
        Collections.shuffle(candidatos);
        candidatos.sort((a, b) -> Integer.compare(b.score, a.score));

        List<Cancion> resultado = new ArrayList<>();
        for (Scored s : candidatos) {
            resultado.add(s.cancion);
            if (resultado.size() >= max) break;
        }

        return resultado;
    }
}
