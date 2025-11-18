package com.syncup.app.logic;

import com.syncup.app.model.Usuario;

import java.util.*;

/**
 * <h2>Grafo Social</h2>
 * Gestiona la red social del sistema: relaciones de "seguir" entre usuarios.
 * 
 * <p>
 * Implementa un grafo dirigido donde cada usuario puede seguir a otros usuarios.
 * Las relaciones se almacenan en archivos CSV individuales y se cargan en memoria
 * para acceso rápido.
 * </p>
 * 
 * <h3>Estructura:</h3>
 * <pre>
 *     usuario → conjunto de usuarios que sigue
 * </pre>
 * 
 * <h3>Funcionalidades:</h3>
 * <ul>
 *     <li>Seguir a un usuario</li>
 *     <li>Dejar de seguir a un usuario</li>
 *     <li>Obtener lista de seguidos de un usuario</li>
 *     <li>Sugerir usuarios no seguidos</li>
 *     <li>Persistencia en archivos CSV</li>
 * </ul>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class GrafoSocial {

    /** Estructura: usuario → conjunto de usuarios que sigue */
    private final Map<String, Set<String>> siguiendo = new HashMap<>();
    /** Ruta donde se guardan los archivos de seguidos */
    private static final String RUTA_CARPETA = "src/main/resources/data/social/";

    /**
     * Constructor por defecto.
     * Crea el directorio social si no existe.
     */
    public GrafoSocial() {
        new java.io.File(RUTA_CARPETA).mkdirs();
    }

    /**
     * Obtiene o crea el conjunto de usuarios que un usuario sigue.
     * 
     * @param u nombre de usuario
     * @return conjunto de usuarios seguidos
     */
    private Set<String> getSet(String u) {
        return siguiendo.computeIfAbsent(u, k -> new HashSet<>());
    }

    // ===========================
    // OPERACIONES SOCIALES
    // ===========================

    /**
     * Permite que un usuario siga a otro usuario.
     * No se permite seguir a uno mismo.
     * 
     * @param usuario usuario que sigue
     * @param objetivo usuario a seguir
     * @return {@code true} si se agregó la relación, {@code false} si ya la seguía
     */
    public boolean seguir(String usuario, String objetivo) {
        if (usuario == null || objetivo == null)
            return false;
        if (usuario.equalsIgnoreCase(objetivo))
            return false;

        Set<String> seg = getSet(usuario);
        if (seg.contains(objetivo))
            return false;

        seg.add(objetivo);
        guardarSeguidos(usuario);
        return true;
    }

    /**
     * Permite que un usuario deje de seguir a otro.
     * 
     * @param usuario usuario que deja de seguir
     * @param objetivo usuario del que dejar de seguir
     * @return {@code true} si se eliminó la relación, {@code false} si no la seguía
     */
    public boolean dejarDeSeguir(String usuario, String objetivo) {
        if (!siguiendo.containsKey(usuario))
            return false;

        boolean ok = siguiendo.get(usuario).remove(objetivo);
        if (ok)
            guardarSeguidos(usuario);

        return ok;
    }

    /**
     * Obtiene la lista de usuarios que un usuario sigue.
     * 
     * @param usuario nombre de usuario
     * @return lista de usuarios que sigue
     */
    public List<String> obtenerSeguidos(String usuario) {
        return new ArrayList<>(getSet(usuario));
    }

    /**
     * Sugiere usuarios que el usuario no sigue aún.
     * 
     * @param usuario nombre de usuario
     * @param limite número máximo de sugerencias
     * @return lista de usuarios sugeridos
     */
    public List<String> sugerirUsuarios(String usuario, int limite) {
        Set<String> yaSigue = getSet(usuario);

        List<String> sugerencias = new ArrayList<>();
        List<Usuario> usuarios = DataStore.getInstance()
                .getUsuarioManager()
                .obtenerUsuarios();

        for (Usuario u : usuarios) {
            String username = u.getUsername();
            if (username.equalsIgnoreCase(usuario))
                continue;
            if (!yaSigue.contains(username)) {
                sugerencias.add(username);
            }
            if (sugerencias.size() >= limite)
                break;
        }

        return sugerencias;
    }

    // ===========================================================
    // PERSISTENCIA
    // ===========================================================

    /**
     * Carga los usuarios seguidos desde un archivo CSV.
     * 
     * @param usuario nombre de usuario
     */
    public void cargarSeguidos(String usuario) {
        try {
            java.io.File archivo = new java.io.File(RUTA_CARPETA + usuario + "_seguidos.csv");
            if (!archivo.exists()) {
                siguiendo.put(usuario, new HashSet<>());
                return;
            }

            Set<String> lista = new HashSet<>();

            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                            new java.io.FileInputStream(archivo),
                            java.nio.charset.StandardCharsets.UTF_8))) {

                String linea;
                while ((linea = br.readLine()) != null) {
                    linea = linea.trim();
                    if (linea.isEmpty())
                        continue;

                    String[] partes = linea.split(",", 2);
                    if (partes.length == 2) {
                        lista.add(partes[1].trim());
                    }
                }
            }

            siguiendo.put(usuario, lista);

        } catch (Exception e) {
            System.err.println("❌ Error al cargar seguidos: " + e.getMessage());
        }
    }

    /**
     * Guarda los usuarios seguidos en un archivo CSV.
     * Formato: usuario,usuario_seguido
     * 
     * @param usuario nombre de usuario
     */
    public void guardarSeguidos(String usuario) {
        try {
            java.io.File archivo = new java.io.File(RUTA_CARPETA + usuario + "_seguidos.csv");
            archivo.getParentFile().mkdirs();

            try (java.io.BufferedWriter bw = new java.io.BufferedWriter(
                    new java.io.OutputStreamWriter(
                            new java.io.FileOutputStream(archivo),
                            java.nio.charset.StandardCharsets.UTF_8))) {

                for (String seguido : obtenerSeguidos(usuario)) {
                    bw.write(usuario + "," + seguido);
                    bw.newLine();
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error guardando seguidos: " + e.getMessage());
        }
    }

}
