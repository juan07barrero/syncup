package com.syncup.app.logic;

import com.syncup.app.model.Usuario;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <h2>UsuarioManager</h2>
 * Gestiona la persistencia y administración de usuarios del sistema SyncUp.
 * <p>
 * Esta clase carga los usuarios desde un archivo CSV, los mantiene en memoria,
 * y permite realizar operaciones como validar credenciales, registrar nuevos usuarios,
 * modificar roles, actualizar perfiles y eliminar cuentas.
 * </p>
 * <h3>Formato del archivo usuarios.csv</h3>
 * <pre>
 * username,password,nombre,rol
 * </pre>
 *
 * <p>
 * La clase actúa como un repositorio en memoria utilizando un {@code Map<String, Usuario>}
 * donde la llave es el nombre de usuario.
 * </p>
 *
 * @author
 *     Juan Barrero
 * @version
 *     1.0
 */
public class UsuarioManager {

    /** Mapa de usuarios cargados en memoria (username → Usuario). */
    private final Map<String, Usuario> usuarios = new HashMap<>();

    /** Ruta del archivo CSV donde se guardan todos los usuarios. */
    private static final String RUTA_USUARIOS = "src/main/resources/data/usuarios.csv";

    /**
     * Constructor.
     * Inicializa el sistema cargando los usuarios existentes desde el archivo CSV.
     */
    public UsuarioManager() {
        cargarUsuarios();
    }

    // =====================================================
    // CARGA / GUARDADO
    // =====================================================

    /**
     * Carga todos los usuarios desde el archivo CSV y los almacena en memoria.
     * Si el archivo no existe, la clase continúa sin generar errores y el archivo
     * será creado automáticamente al guardar usuarios por primera vez.
     */
    private void cargarUsuarios() {
        File archivo = new File(RUTA_USUARIOS);
        if (!archivo.exists()) {
            System.out.println("ℹ️ No existe usuarios.csv, se creará al guardar usuarios.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(archivo), StandardCharsets.UTF_8))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty())
                    continue;

                // Formato: username,password,nombre,rol
                String[] partes = linea.split(",", 4);
                if (partes.length == 4) {

                    String username = partes[0].trim();
                    String password = partes[1].trim();
                    String nombre   = partes[2].trim();
                    String rol      = partes[3].trim();

                    usuarios.put(username, new Usuario(username, password, rol, nombre));
                }
            }

            System.out.println("✅ Usuarios cargados correctamente: " + usuarios.size());
        } catch (IOException e) {
            System.err.println("❌ Error al leer usuarios: " + e.getMessage());
        }
    }

    /**
     * Guarda todos los usuarios actualmente en memoria dentro del archivo CSV.
     * Este método se ejecuta cada vez que se registra un nuevo usuario o se
     * modifica información.
     */
    public void guardarUsuarios() {
        File archivo = new File(RUTA_USUARIOS);
        archivo.getParentFile().mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(archivo), StandardCharsets.UTF_8))) {

            for (Usuario u : usuarios.values()) {
                bw.write(u.getUsername() + "," +
                         u.getPassword() + "," +
                         (u.getNombre() == null ? "" : u.getNombre()) + "," +
                         u.getRol());
                bw.newLine();
            }

            System.out.println("💾 Usuarios guardados: " + usuarios.size());
        } catch (IOException e) {
            System.err.println("❌ Error al guardar usuarios: " + e.getMessage());
        }
    }

    // =====================================================
    // OPERACIONES
    // =====================================================

    /**
     * Valida si un par de credenciales coincide con un usuario registrado.
     *
     * @param username nombre de usuario a validar.
     * @param password contraseña proporcionada.
     * @return {@code true} si las credenciales coinciden, {@code false} en caso contrario.
     */
    public boolean validarCredenciales(String username, String password) {
        Usuario u = usuarios.get(username);
        if (u == null)
            return false;
        return Objects.equals(u.getPassword(), password);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param username nombre único de usuario.
     * @param password contraseña del usuario.
     * @param rol el rol asignado (admin/usuario).
     * @param nombre nombre real del usuario.
     * @return {@code true} si se registró correctamente, {@code false} si ya existía.
     */
    public boolean registrarUsuario(String username, String password, String rol, String nombre) {
        if (usuarios.containsKey(username))
            return false;

        Usuario u = new Usuario(username, password, rol, nombre);
        usuarios.put(username, u);
        guardarUsuarios();
        return true;
    }

    /**
     * Obtiene un usuario por su nombre de usuario.
     *
     * @param username nombre del usuario.
     * @return objeto {@link Usuario} si existe, o {@code null} si no.
     */
    public Usuario getUsuario(String username) {
        return usuarios.get(username);
    }

    /**
     * Devuelve una lista con todos los usuarios del sistema.
     *
     * @return lista de usuarios.
     */
    public List<Usuario> obtenerUsuarios() {
        return new ArrayList<>(usuarios.values());
    }

    /**
     * Devuelve el mapa completo de usuarios (útil para estadísticas).
     *
     * @return mapa username → Usuario.
     */
    public Map<String, Usuario> obtenerTodos() {
        return usuarios;
    }

    /**
     * Elimina un usuario y actualiza el archivo CSV.
     *
     * @param username usuario a eliminar.
     * @return {@code true} si se eliminó, {@code false} si no existía.
     */
    public boolean eliminarUsuario(String username) {
        if (!usuarios.containsKey(username))
            return false;

        usuarios.remove(username);
        guardarUsuarios();
        return true;
    }

    /**
     * Cambia el rol de un usuario (de admin → usuario o viceversa).
     *
     * @param username usuario cuyo rol será modificado.
     * @param nuevoRol nuevo rol asignado.
     * @return {@code true} si se realizó el cambio, {@code false} si el usuario no existe.
     */
    public boolean cambiarRol(String username, String nuevoRol) {
        Usuario u = usuarios.get(username);
        if (u == null)
            return false;

        u.setRol(nuevoRol);
        guardarUsuarios();
        return true;
    }

    /**
     * Actualiza los datos editables de un perfil (nombre y contraseña).
     *
     * @param actualizado objeto Usuario con los nuevos valores.
     */
    public void actualizarPerfil(Usuario actualizado) {
        if (actualizado == null)
            return;

        Usuario u = usuarios.get(actualizado.getUsername());
        if (u == null)
            return;

        u.setNombre(actualizado.getNombre());
        u.setPassword(actualizado.getPassword());

        guardarUsuarios();
    }
}

