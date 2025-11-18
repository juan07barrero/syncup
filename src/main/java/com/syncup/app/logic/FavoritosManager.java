package com.syncup.app.logic;

import com.syncup.app.model.Cancion;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <h2>FavoritosManager</h2>
 * Gestiona las canciones marcadas como favoritas por cada usuario del sistema.
 * <p>
 * Las canciones favoritas se almacenan en archivos CSV individuales con formato:
 * </p>
 * <pre>
 *     titulo,artista,genero
 * </pre>
 * Cada usuario tiene su propio archivo <code>favoritos_[usuario].csv</code>.
 * <p>
 * <b>Funcionalidades:</b>
 * </p>
 * <ul>
 *     <li>Añadir y eliminar favoritos (toggle)</li>
 *     <li>Persistencia en archivos CSV</li>
 *     <li>Obtener lista de favoritos por usuario</li>
 *     <li>Cargar automáticamente al iniciar la aplicación</li>
 * </ul>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class FavoritosManager {

    // usuario -> conjunto de líneas "titulo,artista,genero"
    /** Almacenamiento en memoria de favoritos: usuario → conjunto de canciones favoritas */
    private final Map<String, Set<String>> favoritosPorUsuario = new HashMap<>();

    /** Ruta de la carpeta donde se guardan los archivos de favoritos */
    private static final String RUTA_CARPETA = "src/main/resources/data/favoritos";

    /**
     * Constructor por defecto.
     * Inicializa el directorio de favoritos y carga todos los archivos existentes.
     */
    public FavoritosManager() {
        inicializarDirectorio();
        cargarTodosLosFavoritos();
    }

    /**
     * Crea el directorio de favoritos si no existe.
     */
    private void inicializarDirectorio() {
        File carpeta = new File(RUTA_CARPETA);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
            System.out.println("📁 Carpeta de favoritos creada.");
        }
    }

    // =====================================================
    //   TOGGLE FAVORITO
    // =====================================================

    /**
     * Agrega o elimina una canción de los favoritos del usuario (toggle).
     * Si la canción ya es favorita, la elimina; si no lo es, la agrega.
     * Los cambios se guardan inmediatamente en disco.
     * 
     * @param usuario nombre de usuario
     * @param cancion canción a agregar/eliminar de favoritos
     * @return {@code true} si la canción fue agregada, {@code false} si fue eliminada
     */
    public boolean toggleFavorito(String usuario, Cancion cancion) {
        if (usuario == null || usuario.trim().isEmpty() || cancion == null) return false;

        usuario = usuario.trim();
        favoritosPorUsuario.putIfAbsent(usuario, new HashSet<>());
        Set<String> favs = favoritosPorUsuario.get(usuario);

        String registro = cancion.getTitulo() + "," +
                          cancion.getArtista() + "," +
                          cancion.getGenero();

        boolean agregado;
        if (favs.contains(registro)) {
            favs.remove(registro);
            agregado = false;
        } else {
            favs.add(registro);
            agregado = true;
        }

        guardarFavoritosDe(usuario);
        return agregado;
    }

    // =====================================================
    //   PERSISTENCIA
    // =====================================================

    /**
     * Guarda los favoritos de un usuario en un archivo CSV.
     * Se usa internamente cada vez que se modifica el listado de favoritos.
     * 
     * @param usuario nombre de usuario
     */
    public void guardarFavoritosDe(String usuario) {
        if (usuario == null || usuario.trim().isEmpty()) return;
        usuario = usuario.trim();

        File archivo = new File(RUTA_CARPETA, "favoritos_" + usuario + ".csv");
        archivo.getParentFile().mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(archivo), StandardCharsets.UTF_8))) {

            for (String linea : favoritosPorUsuario.getOrDefault(usuario, Set.of())) {
                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al guardar favoritos de " + usuario + ": " + e.getMessage());
        }
    }

    /**
     * Carga los favoritos de un usuario desde su archivo CSV.
     * 
     * @param usuario nombre de usuario
     */
    public void cargarFavoritos(String usuario) {
        if (usuario == null || usuario.trim().isEmpty()) return;
        usuario = usuario.trim();

        File archivo = new File(RUTA_CARPETA, "favoritos_" + usuario + ".csv");
        Set<String> favs = new HashSet<>();

        if (!archivo.exists()) {
            favoritosPorUsuario.put(usuario, favs);
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(archivo), StandardCharsets.UTF_8))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    favs.add(linea);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error al cargar favoritos de " + usuario + ": " + e.getMessage());
        }

        favoritosPorUsuario.put(usuario, favs);
        System.out.println("🎵 Favoritos cargados para " + usuario + ": " + favs.size());
    }

    /**
     * Alias de {@link #cargarFavoritos(String)} para mayor claridad.
     * Carga los favoritos de un usuario específico.
     * 
     * @param usuario nombre de usuario
     */
    public void cargarFavoritosDeUsuario(String usuario) {
        cargarFavoritos(usuario);
    }

    /**
     * Carga automáticamente todos los archivos de favoritos encontrados.
     * Se ejecuta una sola vez al inicializar el manager.
     */
    private void cargarTodosLosFavoritos() {
        File carpeta = new File(RUTA_CARPETA);
        if (!carpeta.exists()) return;

        File[] archivos = carpeta.listFiles((dir, name) ->
                name.startsWith("favoritos_") && name.endsWith(".csv"));

        if (archivos == null) return;

        for (File archivo : archivos) {
            String usuario = archivo.getName()
                    .replace("favoritos_", "")
                    .replace(".csv", "");
            cargarFavoritos(usuario);
        }
    }

    // =====================================================
    //   CONSULTAS
    // =====================================================

    /**
     * Obtiene el listado de canciones favoritas de un usuario como objetos {@link Cancion}.
     * 
     * @param usuario nombre de usuario
     * @return lista de canciones favoritas, lista vacía si no hay favoritos
     */
    public List<Cancion> obtenerFavoritos(String usuario) {
        List<Cancion> lista = new ArrayList<>();

        File archivo = new File(RUTA_CARPETA, "favoritos_" + usuario + ".csv");
        if (!archivo.exists()) {
            return lista;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(archivo), StandardCharsets.UTF_8))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",", 3);
                if (partes.length == 3) {
                    lista.add(new Cancion(
                            partes[0].trim(),
                            partes[1].trim(),
                            partes[2].trim()
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al leer favoritos de " + usuario + ": " + e.getMessage());
        }

        return lista;
    }

    /**
     * Devuelve el mapa completo de todos los favoritos en memoria.
     * 
     * @return mapa usuario → conjunto de líneas favoritas
     */
    public Map<String, Set<String>> obtenerTodos() {
        return favoritosPorUsuario;
    }

    @Override
    public String toString() {
        return "FavoritosManager{" +
                "favoritosPorUsuario=" + favoritosPorUsuario +
                '}';
    }
}
