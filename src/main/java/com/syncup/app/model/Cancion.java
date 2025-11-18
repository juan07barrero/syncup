package com.syncup.app.model;

import java.util.Objects;

/**
 * <h2>Canción</h2>
 * Representa una canción individual en el sistema SyncUp.
 * 
 * <p>
 * Una canción contiene información básica como título, artista, género, año
 * de lanzamiento y duración en segundos. Cada canción posee un identificador
 * único generado automáticamente a partir de su título.
 * </p>
 * 
 * <h3>Atributos principales:</h3>
 * <ul>
 *     <li><b>id</b>: Identificador único (generado a partir del título en minúsculas)</li>
 *     <li><b>titulo</b>: Nombre de la canción</li>
 *     <li><b>artista</b>: Nombre del artista o banda</li>
 *     <li><b>genero</b>: Género musical (Rock, Pop, Jazz, etc.)</li>
 *     <li><b>anio</b>: Año de lanzamiento</li>
 *     <li><b>duracion</b>: Duración en segundos</li>
 * </ul>
 * 
 * <h3>Uso en el sistema:</h3>
 * Las canciones se usan en:
 * <ul>
 *     <li>La biblioteca musical global</li>
 *     <li>Favoritos del usuario</li>
 *     <li>Playlists</li>
 *     <li>Historial de reproducciones</li>
 * </ul>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class Cancion {

    /** Identificador único de la canción */
    private String id;
    /** Título o nombre de la canción */
    private String titulo;
    /** Nombre del artista o banda */
    private String artista;
    /** Género musical */
    private String genero;
    /** Año de lanzamiento de la canción */
    private int anio;
    /** Duración de la canción en segundos */
    private int duracion;

    /**
     * Constructor simplificado para crear una canción.
     * El ID se genera automáticamente a partir del título.
     * 
     * @param titulo nombre de la canción
     * @param artista nombre del artista
     * @param genero género musical
     */
    public Cancion(String titulo, String artista, String genero) {
        this.id = titulo.toLowerCase().replace(" ", "_");
        this.titulo = titulo;
        this.artista = artista;
        this.genero = genero;
        this.anio = 0;
        this.duracion = 0;
    }

    /**
     * Constructor completo de la canción con todos los parámetros.
     * 
     * @param id identificador único
     * @param titulo nombre de la canción
     * @param artista nombre del artista
     * @param genero género musical
     * @param anio año de lanzamiento
     * @param duracion duración en segundos
     */
    public Cancion(String id, String titulo, String artista, String genero, int anio, int duracion) {
        this.id = id;
        this.titulo = titulo;
        this.artista = artista;
        this.genero = genero;
        this.anio = anio;
        this.duracion = duracion;
    }

    // ===================== GETTERS Y SETTERS =====================

    /**
     * Obtiene el identificador único de la canción.
     * @return el ID de la canción
     */
    public String getId() { return id; }
    
    /**
     * Establece el identificador único de la canción.
     * @param id nuevo identificador
     */
    public void setId(String id) { this.id = id; }

    /**
     * Obtiene el título de la canción.
     * @return el título
     */
    public String getTitulo() { return titulo; }
    
    /**
     * Establece el título de la canción.
     * @param titulo nuevo título
     */
    public void setTitulo(String titulo) { this.titulo = titulo; }

    /**
     * Obtiene el nombre del artista.
     * @return nombre del artista
     */
    public String getArtista() { return artista; }
    
    /**
     * Establece el nombre del artista.
     * @param artista nuevo artista
     */
    public void setArtista(String artista) { this.artista = artista; }

    /**
     * Obtiene el género musical.
     * @return el género
     */
    public String getGenero() { return genero; }
    
    /**
     * Establece el género musical.
     * @param genero nuevo género
     */
    public void setGenero(String genero) { this.genero = genero; }

    /**
     * Obtiene el año de lanzamiento.
     * @return año de lanzamiento
     */
    public int getAnio() { return anio; }
    
    /**
     * Establece el año de lanzamiento.
     * @param anio nuevo año
     */
    public void setAnio(int anio) { this.anio = anio; }

    /**
     * Obtiene la duración de la canción.
     * @return duración en segundos
     */
    public int getDuracion() { return duracion; }
    
    /**
     * Establece la duración de la canción.
     * @param duracion duración en segundos
     */
    public void setDuracion(int duracion) { this.duracion = duracion; }

    // ===================== MÉTODOS EQUALS, HASHCODE Y TOSTRING =====================
    
    /**
     * Compara dos canciones por su identificador único.
     * Dos canciones son iguales si tienen el mismo ID.
     * 
     * @param o objeto a comparar
     * @return {@code true} si las canciones tienen el mismo ID, {@code false} en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cancion cancion = (Cancion) o;
        return Objects.equals(id, cancion.id);
    }

    /**
     * Calcula el código hash de la canción basado en su ID.
     * 
     * @return código hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Devuelve una representación en string de la canción.
     * 
     * @return "Título - Artista"
     */
    @Override
    public String toString() {
        return titulo + " - " + artista;
    }
}
