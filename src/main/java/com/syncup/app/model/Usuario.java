package com.syncup.app.model;

import java.util.Objects;

/**
 * <h2>Usuario</h2>
 * Representa un usuario en el sistema SyncUp.
 * 
 * <p>
 * Cada usuario tiene credenciales de autenticación, un rol (admin o usuario regular)
 * y un nombre para mostrar en la interfaz. Los usuarios pueden tener favoritos,
 * playlists, historial de reproducciones y participar en la red social.
 * </p>
 * 
 * <h3>Atributos:</h3>
 * <ul>
 *     <li><b>username</b>: Identificador único de acceso (no puede duplicarse)</li>
 *     <li><b>password</b>: Contraseña del usuario (encriptada en producción)</li>
 *     <li><b>rol</b>: "admin" para administradores o "usuario" para usuarios normales</li>
 *     <li><b>nombre</b>: Nombre real o nombre a mostrar en la interfaz</li>
 * </ul>
 * 
 * <h3>Roles disponibles:</h3>
 * <ul>
 *     <li><b>admin</b>: Acceso completo al sistema, puede gestionar usuarios y contenido</li>
 *     <li><b>usuario</b>: Acceso limitado, puede crear playlists y marcar favoritos</li>
 * </ul>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class Usuario {

    /** Nombre de usuario único para acceso al sistema */
    private String username;
    /** Contraseña del usuario */
    private String password;
    /** Rol del usuario: "admin" o "usuario" */
    private String rol;
    /** Nombre real o nombre a mostrar en la interfaz */
    private String nombre;

    /**
     * Constructor simplificado de usuario.
     * El nombre se asigna por defecto igual al username.
     * 
     * @param username nombre de usuario único
     * @param password contraseña del usuario
     * @param rol rol del usuario ("admin" o "usuario")
     */
    public Usuario(String username, String password, String rol) {
        this(username, password, rol, username);
    }

    /**
     * Constructor completo de usuario.
     * 
     * @param username nombre de usuario único
     * @param password contraseña del usuario
     * @param rol rol del usuario ("admin" o "usuario")
     * @param nombre nombre real o nombre a mostrar
     */
    public Usuario(String username, String password, String rol, String nombre) {
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.nombre = nombre;
    }

    // ===================== GETTERS Y SETTERS =====================

    /**
     * Obtiene el nombre de usuario único.
     * @return nombre de usuario
     */
    public String getUsername() {
        return username;
    }

    /**
     * Establece el nombre de usuario.
     * @param username nuevo nombre de usuario
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Obtiene la contraseña del usuario.
     * @return contraseña
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña del usuario.
     * @param password nueva contraseña
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtiene el rol del usuario.
     * @return rol ("admin" o "usuario")
     */
    public String getRol() {
        return rol;
    }

    /**
     * Establece el rol del usuario.
     * @param rol nuevo rol ("admin" o "usuario")
     */
    public void setRol(String rol) {
        this.rol = rol;
    }

    /**
     * Obtiene el nombre real o nombre a mostrar del usuario.
     * @return nombre del usuario
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre real o nombre a mostrar del usuario.
     * @param nombre nuevo nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // ===================== MÉTODOS EQUALS, HASHCODE Y TOSTRING =====================

    /**
     * Compara dos usuarios por su nombre de usuario único.
     * 
     * @param o objeto a comparar
     * @return {@code true} si tienen el mismo username
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(username, usuario.username);
    }

    /**
     * Calcula el código hash del usuario basado en su username.
     * 
     * @return código hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    /**
     * Devuelve una representación en string del usuario.
     * 
     * @return representación string del usuario
     */
    @Override
    public String toString() {
        return "Usuario{" +
                "username='" + username + '\'' +
                ", rol='" + rol + '\'' +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}
