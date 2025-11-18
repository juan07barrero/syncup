package com.syncup.app.logic;

import com.syncup.app.model.BibliotecaMusical;
import com.syncup.app.model.Usuario;

/**
 * <b>DataStore</b> es el punto central de acceso a toda la lógica de negocio del sistema.
 * Implementa el patrón <b>Singleton</b>, garantizando que exista una única instancia
 * utilizada por toda la aplicación.
 *
 * Desde aquí se manejan:
 * <ul>
 *     <li>Gestión de usuarios (UsuarioManager)</li>
 *     <li>Gestión de favoritos</li>
 *     <li>Gestión de playlists</li>
 *     <li>Historial global y por usuario</li>
 *     <li>Biblioteca musical</li>
 *     <li>Recomendaciones</li>
 *     <li>Red social (seguir / dejar de seguir)</li>
 * </ul>
 *
 * También administra el concepto de “usuario activo” para la sesión actual.
 */
public class DataStore {

    /** Instancia única del Singleton */
    private static DataStore instance;

    /** Manejador de usuarios */
    private final UsuarioManager usuarioManager;

    /** Manejador de favoritos */
    private final FavoritosManager favoritosManager;

    /** Manejador de playlists */
    private final PlaylistManager playlistManager;

    /** Manejador del historial global */
    private final HistorialManager historialManager;

    /** Biblioteca musical global */
    private final BibliotecaMusical biblioteca;

    /** Grafo social (seguir/dejar de seguir usuarios) */
    private final GrafoSocial grafoSocial;

    /** Recomendador de canciones basado en similitud */
    private final Recomendador recomendador;

    /** Nombre del usuario actualmente autenticado (null si no hay sesión) */
    private String usuarioActivo;

    /**
     * Constructor privado: inicializa todos los módulos.
     * El sistema carga usuarios, favoritos, playlists, historial y biblioteca.
     */
    private DataStore() {
        System.out.println("📦 Inicializando DataStore...");

        usuarioManager = new UsuarioManager();
        favoritosManager = new FavoritosManager();
        playlistManager = new PlaylistManager();
        historialManager = new HistorialManager();
        biblioteca = new BibliotecaMusical();
        recomendador = new Recomendador(biblioteca, favoritosManager);
        grafoSocial = new GrafoSocial();

        usuarioActivo = null;

        System.out.println("📁 DataStore inicializado correctamente.");
    }

    /**
     * Devuelve la instancia única del DataStore.
     * Si no existe, la crea.
     *
     * @return instancia del DataStore
     */
    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // =====================================================
    // GETTERS DE MANAGERS
    // =====================================================

    /** @return manejador de usuarios */
    public UsuarioManager getUsuarioManager() {
        return usuarioManager;
    }

    /** @return manejador de favoritos */
    public FavoritosManager getFavoritos() {
        return favoritosManager;
    }

    /** @return manejador de playlists */
    public PlaylistManager getPlaylists() {
        return playlistManager;
    }

    /** @return manejador del historial de reproducciones */
    public HistorialManager getHistorial() {
        return historialManager;
    }

    /** @return biblioteca musical global */
    public BibliotecaMusical getBiblioteca() {
        return biblioteca;
    }

    /** @return grafo social (seguidores/seguidos) */
    public GrafoSocial getGrafoSocial() {
        return grafoSocial;
    }

    /** @return recomendador de canciones */
    public Recomendador getRecomendador() {
        return recomendador;
    }

    // =====================================================
    // USUARIO ACTIVO / SESIÓN
    // =====================================================

    /**
     * Establece el usuario activo en la sesión actual.
     * También carga automáticamente:
     * <ul>
     *     <li>Favoritos del usuario</li>
     *     <li>Playlists del usuario</li>
     *     <li>Red social del usuario</li>
     * </ul>
     *
     * @param username nombre de usuario autenticado
     */
    public void setUsuarioActivo(String username) {
        this.usuarioActivo = username;

        if (username != null && !username.isEmpty()) {
            System.out.println("🔐 Usuario activo: " + username);

            favoritosManager.cargarFavoritosDeUsuario(username);
            playlistManager.cargarPlaylists(username);
            grafoSocial.cargarSeguidos(username);

            System.out.println("✔ Datos del usuario cargados (favoritos + playlists).");
        }
    }

    /**
     * @return nombre del usuario actualmente autenticado o null si no hay sesión
     */
    public String getUsuarioActivo() {
        return usuarioActivo;
    }

    /**
     * @return rol del usuario activo (admin/usuario) o "invitado" si no hay sesión
     */
    public String getRolUsuarioActivo() {
        if (usuarioActivo == null)
            return "invitado";
        Usuario u = usuarioManager.getUsuario(usuarioActivo);
        return (u != null) ? u.getRol() : "invitado";
    }

    /**
     * @return objeto Usuario correspondiente al usuario activo, o null si no hay ninguno
     */
    public Usuario getUsuarioActivoObj() {
        if (usuarioActivo == null)
            return null;
        return usuarioManager.getUsuario(usuarioActivo);
    }

    // =====================================================
    // AUTENTICACIÓN
    // =====================================================

    /**
     * Valida si un usuario y contraseña coinciden con el CSV cargado.
     *
     * @param username nombre de usuario
     * @param password contraseña
     * @return true si las credenciales son correctas
     */
    public boolean validarUsuario(String username, String password) {
        return usuarioManager.validarCredenciales(username, password);
    }

    /**
     * Registra un usuario nuevo en el sistema.
     *
     * @param username nuevo nombre de usuario
     * @param password contraseña
     * @param rol rol del usuario ("admin" o "usuario")
     * @param nombre nombre real del usuario
     * @return true si se registró correctamente
     */
    public boolean registrarUsuario(String username, String password, String rol, String nombre) {
        return usuarioManager.registrarUsuario(username, password, rol, nombre);
    }

    /**
     * Devuelve todos los usuarios registrados.
     *
     * @return lista de usuarios
     */
    public java.util.List<Usuario> obtenerUsuarios() {
        return usuarioManager.obtenerUsuarios();
    }

    // =====================================================
    // GUARDAR TODO
    // =====================================================

    /**
     * Guarda absolutamente toda la información persistente del sistema:
     * <ul>
     *     <li>Usuarios</li>
     *     <li>Historial global</li>
     *     <li>Favoritos del usuario activo</li>
     *     <li>Playlists del usuario activo</li>
     * </ul>
     * Se llama normalmente al cerrar la aplicación.
     */
    public void guardarTodo() {
        try {
            System.out.println("💾 Guardando DataStore...");

            usuarioManager.guardarUsuarios();
            historialManager.guardarHistorialCSV();

            if (usuarioActivo != null) {
                favoritosManager.guardarFavoritosDe(usuarioActivo);
                playlistManager.guardarPlaylistsDe(usuarioActivo);
            }

            System.out.println("✔ Guardado completo.");
        } catch (Exception e) {
            System.err.println("❌ Error guardando DataStore: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "DataStore{" +
                "usuarioActivo='" + usuarioActivo + '\'' +
                '}';
    }
}
