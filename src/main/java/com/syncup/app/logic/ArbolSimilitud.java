package com.syncup.app.logic;

import com.syncup.app.model.Cancion;

import java.util.*;

/**
 * <h2>Árbol de Similitud</h2>
 * Estructura jerárquica para almacenar y recomendar canciones similares.
 * <p>
 * Organiza las canciones en dos niveles:
 * </p>
 * <ol>
 *     <li><b>Género</b>: Primera clasificación</li>
 *     <li><b>Artista</b>: Segunda clasificación dentro del género</li>
 * </ol>
 * <p>
 * <b>Utilidad:</b> Este árbol permite generar recomendaciones de canciones similares
 * basándose primero en el mismo artista, luego en el mismo género.
 * </p>
 * <p>
 * Ejemplo:
 * </p>
 * <pre>
 *   Nodo(Género="Rock")
 *   ├─ Nodo(Artista="Queen")
 *   │  ├─ Canción: "Bohemian Rhapsody"
 *   │  └─ Canción: "We Will Rock You"
 *   └─ Nodo(Artista="Pink Floyd")
 *      ├─ Canción: "Comfortably Numb"
 *      └─ Canción: "Time"
 * </pre>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class ArbolSimilitud {

    /**
     * Nodo de género.
     * Contiene todos los artistas que tienen canciones en ese género.
     */
    private static class NodoGenero {
        /** Nombre del género */
        String genero;
        /** Mapa de artistas para este género */
        Map<String, NodoArtista> artistas = new HashMap<>();

        /**
         * Crea un nodo de género.
         * @param genero nombre del género
         */
        NodoGenero(String genero) {
            this.genero = genero;
        }
    }

    /**
     * Nodo de artista.
     * Contiene todas las canciones de un artista dentro de un género.
     */
    private static class NodoArtista {
        /** Nombre del artista */
        String artista;
        /** Lista de canciones del artista */
        List<Cancion> canciones = new ArrayList<>();

        /**
         * Crea un nodo de artista.
         * @param artista nombre del artista
         */
        NodoArtista(String artista) {
            this.artista = artista;
        }
    }

    /** Raíz del árbol: mapa de todos los géneros */
    private final Map<String, NodoGenero> generos = new HashMap<>();

    /**
     * Inserta una canción en el árbol de similitud.
     * La canción se organiza jerárquicamente por género → artista.
     * 
     * @param c canción a insertar
     */
    public void insertar(Cancion c) {
        String genero = c.getGenero().toLowerCase();
        String artista = c.getArtista().toLowerCase();

        NodoGenero nodoGenero = generos.computeIfAbsent(genero, g -> new NodoGenero(g));
        NodoArtista nodoArtista = nodoGenero.artistas.computeIfAbsent(artista, a -> new NodoArtista(a));

        nodoArtista.canciones.add(c);
    }

    /**
     * Recomienda canciones similares a una canción base.
     * <p>
     * El algoritmo funciona en dos fases:
     * </p>
     * <ol>
     *     <li>Primero busca canciones del <b>mismo artista y género</b></li>
     *     <li>Si faltan, busca canciones del <b>mismo género pero otros artistas</b></li>
     * </ol>
     * 
     * @param base canción base para buscar similares
     * @param limite número máximo de recomendaciones
     * @return lista de canciones similares
     */
    public List<Cancion> recomendar(Cancion base, int limite) {
        List<Cancion> resultado = new ArrayList<>();

        if (base == null) return resultado;

        String g = base.getGenero().toLowerCase();
        String a = base.getArtista().toLowerCase();

        // 1️⃣ Primero, canciones del mismo artista
        if (generos.containsKey(g)) {
            NodoGenero nodoG = generos.get(g);

            if (nodoG.artistas.containsKey(a)) {
                for (Cancion c : nodoG.artistas.get(a).canciones) {
                    if (!c.getTitulo().equalsIgnoreCase(base.getTitulo())) {
                        resultado.add(c);
                        if (resultado.size() >= limite) return resultado;
                    }
                }
            }

            // 2️⃣ Si faltan, canciones del mismo género (otros artistas)
            for (NodoArtista nodoA : nodoG.artistas.values()) {
                if (nodoA.artista.equalsIgnoreCase(a)) continue;
                for (Cancion c : nodoA.canciones) {
                    if (!c.getTitulo().equalsIgnoreCase(base.getTitulo())) {
                        resultado.add(c);
                        if (resultado.size() >= limite) return resultado;
                    }
                }
            }
        }

        return resultado;
    }

}
