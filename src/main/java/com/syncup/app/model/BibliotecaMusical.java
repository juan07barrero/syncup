package com.syncup.app.model;

import com.syncup.app.logic.Trie;
import com.syncup.app.model.Cancion;
import com.syncup.app.model.estructuras.ListaCanciones;
import com.syncup.app.logic.ArbolSimilitud;
import com.syncup.app.logic.BKTree;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>BibliotecaMusical</h2>
 * Gestiona la colección global de canciones disponibles en el sistema SyncUp.
 * <p>
 * Almacena todas las canciones disponibles y proporciona funcionalidades de:
 * </p>
 * <ul>
 *     <li>Carga y persistencia de canciones desde/hacia CSV</li>
 *     <li>Búsqueda rápida por título</li>
 *     <li>Autocompletado mediante TRIE</li>
 *     <li>Recomendaciones de canciones similares usando árbol de similitud</li>
 *     <li>Búsqueda fuzzy mediante BK-Tree</li>
 * </ul>
 * <p>
 * <b>Estructuras de indexación:</b>
 * </p>
 * <ul>
 *     <li><b>ListaCanciones</b>: almacenamiento principal</li>
 *     <li><b>Trie</b>: para autocompletado rápido</li>
 *     <li><b>BKTree</b>: para búsqueda de similares por distancia Levenshtein</li>
 *     <li><b>ArbolSimilitud</b>: para recomendaciones por género/artista</li>
 * </ul>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class BibliotecaMusical {

    /** Almacenamiento principal de canciones */
    private final ListaCanciones canciones;
    /** Ruta del archivo CSV de canciones */
    private static final String CSV_PATH = "src/main/resources/data/canciones.csv";
    /** Índice de prefijos para autocompletado */
    private final Trie trie = new Trie();
    /** Árbol BK para búsqueda de similares */
    private final BKTree bkTree = new BKTree();
    /** Árbol de similitud por género/artista */
    private final ArbolSimilitud arbolSimilitud = new ArbolSimilitud();

    /**
     * Constructor por defecto.
     * Inicializa las estructuras de datos y carga las canciones desde CSV.
     */
    public BibliotecaMusical() {
        this.canciones = new ListaCanciones();
        cargarDesdeCSV();
    }

    /**
     * Carga todas las canciones desde el archivo CSV al iniciar.
     * También indexa las canciones en los árboles de búsqueda (Trie, BK-Tree, ArbolSimilitud).
     */
    private void cargarDesdeCSV() {
        File archivo = new File(CSV_PATH);
        if (!archivo.exists()) {
            crearArchivoInicial();
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(CSV_PATH), StandardCharsets.UTF_8))) {

            String linea;
            reader.readLine(); // Saltar encabezado

            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",", -1);
                if (partes.length >= 3) {
                    Cancion c = new Cancion(partes[0].trim(), partes[1].trim(), partes[2].trim());
                    canciones.agregarAlFinal(c);
                    arbolSimilitud.insertar(c);

                    // TRIE para autocompletado
                    trie.insertar(c.getTitulo(), c.getTitulo());
                    trie.insertar(c.getArtista(), c.getTitulo());
                    trie.insertar(c.getGenero(), c.getTitulo());

                    // BK-Tree para similitud
                    bkTree.insertar(construirClave(c), c.getTitulo());
                }
            }

            System.out.println("✅ Biblioteca cargada correctamente. Total: " + canciones.getTamaño());

        } catch (IOException e) {
            System.out.println("⚠️ No se pudo cargar el CSV: " + e.getMessage());
        }
    }

    /**
     * Si no existe el archivo canciones.csv, crea uno con algunas canciones base.
     */
    private void crearArchivoInicial() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_PATH))) {
            writer.write("titulo,artista,genero\n");
            writer.write("Shape of You,Ed Sheeran,Pop\n");
            writer.write("Blinding Lights,The Weeknd,Synthwave\n");
            writer.write("Bohemian Rhapsody,Queen,Rock\n");
            writer.write("Someone Like You,Adele,Soul\n");
            writer.write("Smells Like Teen Spirit,Nirvana,Grunge\n");
            System.out.println("🎶 Archivo canciones.csv creado con canciones iniciales.");
        } catch (IOException e) {
            System.out.println("❌ Error al crear archivo inicial: " + e.getMessage());
        }
    }

    // Clave de similitud para el BK-Tree: título + artista + género
    /**
     * Construye una clave para indexar en BK-Tree.
     * La clave es la combinación de título + artista + género en minúsculas.
     * 
     * @param c canción
     * @return clave de similitud
     */
    private String construirClave(Cancion c) {
        String t = c.getTitulo() != null ? c.getTitulo() : "";
        String a = c.getArtista() != null ? c.getArtista() : "";
        String g = c.getGenero() != null ? c.getGenero() : "";
        return (t + " " + a + " " + g).toLowerCase();
    }

    /**
     * Guarda todas las canciones actuales en el archivo CSV.
     */
    public void guardarEnCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_PATH, false))) {
            writer.write("titulo,artista,genero\n");
            for (int i = 0; i < canciones.getTamaño(); i++) {
                Cancion c = canciones.obtenerPorIndice(i);
                writer.write(c.getTitulo() + "," + c.getArtista() + "," + c.getGenero() + "\n");
            }
            System.out.println("💾 Biblioteca guardada correctamente en CSV.");
        } catch (IOException e) {
            System.out.println("❌ Error al guardar CSV: " + e.getMessage());
        }
    }

    /**
     * Agrega una nueva canción a la biblioteca global.
     * La canción se indexa automáticamente en todos los árboles de búsqueda.
     * 
     * @param c canción a agregar
     */
    public void agregarCancion(Cancion c) {
        if (c == null)
            return;
        canciones.agregarAlFinal(c);
        trie.insertar(c.getTitulo(), c.getTitulo());
        trie.insertar(c.getArtista(), c.getTitulo());
        trie.insertar(c.getGenero(), c.getTitulo());
        bkTree.insertar(construirClave(c), c.getTitulo());
        guardarEnCSV();
        arbolSimilitud.insertar(c);

    }

    /**
     * Obtiene sugerencias de autocompletado basadas en un prefijo.
     * 
     * @param prefijo inicio de la búsqueda
     * @return lista de títulos que coinciden con el prefijo
     */
    public List<String> autocompletar(String prefijo) {
        return trie.buscarPorPrefijo(prefijo);
    }

    /**
     * Elimina una canción de la biblioteca por su título.
     * 
     * @param titulo título de la canción a eliminar
     */
    public void eliminarCancion(String titulo) {
        if (titulo == null || titulo.isEmpty())
            return;
        canciones.eliminarPorTitulo(titulo);
        guardarEnCSV();
    }

    /**
     * Busca una canción específica por su título (búsqueda lineal).
     * 
     * @param titulo título a buscar
     * @return canción si se encuentra, null en caso contrario
     */
    public Cancion buscarPorTitulo(String titulo) {
        return canciones.buscarPorTitulo(titulo);
    }

    /**
     * Obtiene todas las canciones como una lista Java.
     * Útil para interfaces, recomendadores, etc.
     * 
     * @return lista de todas las canciones
     */
    public List<Cancion> obtenerTodas() {
        List<Cancion> lista = new ArrayList<>();
        for (int i = 0; i < canciones.getTamaño(); i++) {
            lista.add(canciones.obtenerPorIndice(i));
        }
        return lista;
    }

    /**
     * Elimina una canción especificada por objeto.
     * 
     * @param c canción a eliminar
     */
    public void eliminarCancion(Cancion c) {
        if (c == null)
            return;
        canciones.eliminarPorTitulo(c.getTitulo()); // elimina por el título del objeto
        guardarEnCSV();
    }

    /**
     * Reconstruye el índice Trie desde cero.
     * Se usa cuando se han modificado las canciones de forma directa.
     */
    public void reconstruirTrie() {
        // Limpia el trie (asegúrate de tener este método en Trie)
        trie.clear();

        // Recorrer la lista de canciones usando índices
        for (int i = 0; i < canciones.getTamaño(); i++) {
            Cancion c = canciones.obtenerPorIndice(i);

            if (c == null)
                continue;

            if (c.getTitulo() != null && !c.getTitulo().isEmpty()) {
                trie.insertar(c.getTitulo(), c.getTitulo());
            }
            if (c.getArtista() != null && !c.getArtista().isEmpty()) {
                trie.insertar(c.getArtista(), c.getTitulo());
            }
            if (c.getGenero() != null && !c.getGenero().isEmpty()) {
                trie.insertar(c.getGenero(), c.getTitulo());
            }
        }
    }

    /**
     * Recomienda canciones similares a una canción base.
     * 
     * <p>
     * Utiliza el árbol de similitud que busca canciones del mismo género y artista.
     * </p>
     * 
     * @param base canción de referencia
     * @param maxDistancia parámetro no utilizado (la similitud se basa en género/artista)
     * @param maxResultados límite de recomendaciones
     * @return lista de canciones similares
     */
    public List<Cancion> recomendarSimilares(Cancion base, int maxDistancia, int maxResultados) {
        List<Cancion> resultado = new ArrayList<>();

        if (base == null)
            return resultado;

        // Usamos el árbol de similitud (ignora maxDistancia porque ya no lo
        // necesitamos)
        List<Cancion> sugerencias = arbolSimilitud.recomendar(base, maxResultados);

        // Evitar recomendar la misma canción
        for (Cancion c : sugerencias) {
            if (!c.getTitulo().equalsIgnoreCase(base.getTitulo())) {
                resultado.add(c);
                if (resultado.size() >= maxResultados)
                    break;
            }
        }

        return resultado;
    }

    /**
     * Imprime la lista completa de canciones en la consola.
     */
    public void imprimirBiblioteca() {
        canciones.imprimirLista();
    }
}
