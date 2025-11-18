package com.syncup.app.logic;

import com.syncup.app.model.BibliotecaMusical;
import com.syncup.app.model.Cancion;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <h2>PlaylistManager</h2>
 * Gestiona las playlists por usuario dentro del sistema.
 * Cada usuario puede tener múltiples playlists, y cada playlist puede contener
 * una lista de objetos {@link Cancion}.
 * <p>
 * Las playlists se guardan de forma persistente como archivos CSV individuales
 * con formato:
 * <pre>
 *     usuario_nombrePlaylist.csv
 *     titulo,artista,genero
 * </pre>
 *
 * Estructura manejada en memoria:
 * <pre>
 *     usuario -> (playlist -> lista de canciones)
 * </pre>
 *
 * Este componente es utilizado por los controladores de usuario para:
 * <ul>
 *     <li>Crear playlists</li>
 *     <li>Agregar canciones (por objeto o título)</li>
 *     <li>Listar playlists</li>
 *     <li>Eliminar playlists</li>
 *     <li>Cargar playlists desde CSV</li>
 *     <li>Guardar los cambios</li>
 * </ul>
 */
public class PlaylistManager {

    /** Mapa principal: usuario → (nombrePlaylist → lista de canciones) */
    private final Map<String, Map<String, List<Cancion>>> playlistsPorUsuario;

    /** Ruta base donde se guardan los archivos CSV */
    private static final String RUTA_CARPETA = "src/main/resources/data/playlists";

    /**
     * Constructor por defecto.
     * Inicializa la estructura en memoria y asegura la existencia del directorio.
     */
    public PlaylistManager() {
        this.playlistsPorUsuario = new HashMap<>();
        inicializarDirectorio();
    }

    /**
     * Crea el directorio raíz si no existe.
     * Evita errores cuando se intenta escribir archivos.
     */
    private void inicializarDirectorio() {
        File carpeta = new File(RUTA_CARPETA);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
            System.out.println("📁 Carpeta de playlists creada.");
        }
    }

    // =====================================================
    // MÉTODOS PRINCIPALES
    // =====================================================

    /**
     * Obtiene todas las playlists de un usuario.
     * Si el usuario no tiene playlists aún, se crean automáticamente.
     *
     * @param usuario nombre de usuario
     * @return mapa playlist → lista de canciones
     */
    public Map<String, List<Cancion>> getPlaylistsDe(String usuario) {
        return playlistsPorUsuario.computeIfAbsent(usuario, k -> new HashMap<>());
    }

    /**
     * Crea una playlist vacía para el usuario.
     *
     * @param usuario nombre de usuario
     * @param nombre nombre de la playlist
     * @return true si se creó correctamente, false si ya existía
     */
    public boolean crearPlaylist(String usuario, String nombre) {
        Map<String, List<Cancion>> pls = getPlaylistsDe(usuario);
        if (pls.containsKey(nombre))
            return false;

        pls.put(nombre, new ArrayList<>());
        guardarPlaylistsDe(usuario);
        return true;
    }

    /**
     * Agrega una canción a una playlist usando un objeto {@link Cancion}.
     *
     * @param usuario nombre de usuario
     * @param playlist nombre de la playlist
     * @param c canción a agregar
     * @return true si se agregó correctamente
     */
    public boolean agregarCancion(String usuario, String playlist, Cancion c) {
        Map<String, List<Cancion>> pls = getPlaylistsDe(usuario);
        if (!pls.containsKey(playlist))
            return false;

        pls.get(playlist).add(c);
        guardarPlaylistsDe(usuario);
        return true;
    }

    /**
     * Elimina una playlist completa del usuario, tanto en memoria como en disco.
     *
     * @param usuario nombre de usuario
     * @param nombre nombre de la playlist
     * @return true si la playlist existía y fue eliminada
     */
    public boolean eliminarPlaylist(String usuario, String nombre) {
        Map<String, List<Cancion>> pls = getPlaylistsDe(usuario);
        if (!pls.containsKey(nombre))
            return false;

        pls.remove(nombre);

        File archivo = new File(RUTA_CARPETA, usuario + "_" + nombre + ".csv");
        if (archivo.exists())
            archivo.delete();

        guardarPlaylistsDe(usuario);
        return true;
    }

    /**
     * Elimina todas las playlists de un usuario.
     * Utilizado cuando un usuario es borrado del sistema.
     *
     * @param usuario nombre de usuario
     */
    public void eliminarTodoUsuario(String usuario) {
        playlistsPorUsuario.remove(usuario);

        File carpeta = new File(RUTA_CARPETA);
        File[] archivos = carpeta.listFiles((dir, name) -> name.startsWith(usuario + "_"));

        if (archivos != null) {
            for (File a : archivos)
                a.delete();
        }
    }

    // =====================================================
    // PERSISTENCIA
    // =====================================================

    /**
     * Carga todas las playlists del usuario desde la carpeta de CSV.
     *
     * @param usuario nombre de usuario
     */
    public void cargarPlaylists(String usuario) {

        if (usuario == null) {
            System.out.println("⚠️ cargarPlaylists llamado con usuario NULL");
            return;
        }

        String usuarioFinal = usuario.trim();
        if (usuarioFinal.isEmpty()) {
            System.out.println("⚠️ cargarPlaylists llamado con usuario vacío");
            return;
        }

        playlistsPorUsuario.putIfAbsent(usuarioFinal, new HashMap<>());

        File carpeta = new File(RUTA_CARPETA);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
            System.out.println("📁 Carpeta creada: " + carpeta.getAbsolutePath());
            return;
        }

        File[] archivos = carpeta.listFiles((dir, name) ->
                name != null && name.startsWith(usuarioFinal + "_") && name.endsWith(".csv"));

        if (archivos == null || archivos.length == 0) {
            System.out.println("ℹ️ No hay playlists para " + usuarioFinal);
            return;
        }

        for (File archivo : archivos) {

            String nombre = archivo.getName()
                    .replace(usuarioFinal + "_", "")
                    .replace(".csv", "");

            List<Cancion> canciones = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    String[] p = linea.split(",", 3);
                    if (p.length == 3) {
                        canciones.add(new Cancion(
                                p[0].trim(), p[1].trim(), p[2].trim()));
                    }
                }
            } catch (IOException e) {
                System.err.println("❌ Error leyendo playlist " + archivo.getName() + ": " + e.getMessage());
            }

            playlistsPorUsuario.get(usuarioFinal).put(nombre, canciones);
        }

        System.out.println("🎶 Playlists cargadas para " + usuarioFinal
                + ": " + playlistsPorUsuario.get(usuarioFinal).size());
    }

    /**
     * Guarda todas las playlists de un usuario en archivos CSV individuales.
     *
     * @param usuario nombre de usuario
     */
    public void guardarPlaylistsDe(String usuario) {
        if (usuario == null || usuario.trim().isEmpty())
            return;

        Map<String, List<Cancion>> pls = getPlaylistsDe(usuario);

        for (var entry : pls.entrySet()) {
            String nombre = entry.getKey();
            List<Cancion> canciones = entry.getValue();

            File archivo = new File(RUTA_CARPETA, usuario + "_" + nombre + ".csv");

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(archivo), StandardCharsets.UTF_8))) {

                for (Cancion c : canciones) {
                    bw.write(c.getTitulo() + "," + c.getArtista() + "," + c.getGenero());
                    bw.newLine();
                }

            } catch (IOException e) {
                System.err.println("❌ Error guardando playlist " + nombre + ": " + e.getMessage());
            }
        }
    }

    /**
     * Guarda todas las playlists de todos los usuarios.
     * Se usa al cerrar la aplicación.
     */
    public void guardarTodas() {
        for (String usuario : playlistsPorUsuario.keySet()) {
            guardarPlaylistsDe(usuario);
        }
        System.out.println("💾 Todas las playlists guardadas correctamente.");
    }

    // =====================================================
    // MÉTODOS UTILIZADOS POR UsuarioController
    // =====================================================

    /**
     * Obtiene únicamente los títulos de las canciones de una playlist.
     *
     * @param usuario nombre de usuario
     * @param playlist nombre de playlist
     * @return lista de títulos
     */
    public List<String> obtenerTitulosDePlaylist(String usuario, String playlist) {
        Map<String, List<Cancion>> pls = getPlaylistsDe(usuario);

        if (!pls.containsKey(playlist))
            return Collections.emptyList();

        List<String> titulos = new ArrayList<>();
        for (Cancion c : pls.get(playlist)) {
            titulos.add(c.getTitulo());
        }
        return titulos;
    }

    /**
     * Lista los nombres de todas las playlists de un usuario.
     *
     * @param usuario nombre de usuario
     * @return lista de nombres
     */
    public List<String> listarPlaylists(String usuario) {
        return new ArrayList<>(getPlaylistsDe(usuario).keySet());
    }

    /**
     * Agrega una canción buscándola por título en la biblioteca global.
     * Usado por la interfaz de usuario.
     *
     * @param usuario nombre de usuario
     * @param playlist nombre de la playlist
     * @param tituloCancion título de la canción a agregar
     * @return true si la canción existía en la biblioteca y fue agregada
     */
    public boolean agregarCancion(String usuario, String playlist, String tituloCancion) {
        Map<String, List<Cancion>> pls = getPlaylistsDe(usuario);
        if (!pls.containsKey(playlist))
            return false;

        BibliotecaMusical biblioteca = new BibliotecaMusical();
        for (Cancion c : biblioteca.obtenerTodas()) {
            if (c.getTitulo().equalsIgnoreCase(tituloCancion)) {
                pls.get(playlist).add(c);
                guardarPlaylistsDe(usuario);
                return true;
            }
        }
        return false;
    }
}
