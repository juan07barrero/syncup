package com.syncup.app.model.estructuras;

import com.syncup.app.model.Cancion;

/**
 * <h2>Lista Enlazada de Canciones</h2>
 * Estructura de datos que implementa una lista enlazada simple para almacenar objetos Cancion.
 * <p>
 * Esta estructura proporciona operaciones eficientes para:
 * </p>
 * <ul>
 *     <li>Agregar canciones al final de la lista</li>
 *     <li>Buscar canciones por título</li>
 *     <li>Eliminar canciones por título</li>
 *     <li>Acceso por índice con validación</li>
 *     <li>Consulta del tamaño de la lista</li>
 * </ul>
 * <p>
 * <b>Complejidad de operaciones:</b>
 * </p>
 * <ul>
 *     <li><b>Agregar al final</b>: O(n) donde n es el tamaño</li>
 *     <li><b>Búsqueda por título</b>: O(n)</li>
 *     <li><b>Eliminación por título</b>: O(n)</li>
 *     <li><b>Acceso por índice</b>: O(n)</li>
 * </ul>
 * <p>
 * <b>Nodo interno (Privado):</b>
 * </p>
 * <pre>
 *     private class Nodo {
 *         Cancion cancion;
 *         Nodo siguiente;
 *     }
 * </pre>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class ListaCanciones {

    // Nodo interno de la lista
    private class Nodo {
        Cancion cancion;
        Nodo siguiente;

        Nodo(Cancion cancion) {
            this.cancion = cancion;
            this.siguiente = null;
        }
    }

    private Nodo cabeza;
    private int tamaño;

    public ListaCanciones() {
        cabeza = null;
        tamaño = 0;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    public int getTamaño() {
        return tamaño;
    }

    public void agregarAlFinal(Cancion cancion) {
        Nodo nuevo = new Nodo(cancion);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Nodo temp = cabeza;
            while (temp.siguiente != null) {
                temp = temp.siguiente;
            }
            temp.siguiente = nuevo;
        }
        tamaño++;
    }

    public void eliminarPorTitulo(String titulo) {
        if (cabeza == null) return;

        if (cabeza.cancion.getTitulo().equalsIgnoreCase(titulo)) {
            cabeza = cabeza.siguiente;
            tamaño--;
            return;
        }

        Nodo actual = cabeza;
        Nodo anterior = null;

        while (actual != null && !actual.cancion.getTitulo().equalsIgnoreCase(titulo)) {
            anterior = actual;
            actual = actual.siguiente;
        }

        if (actual != null) {
            anterior.siguiente = actual.siguiente;
            tamaño--;
        }
    }

    public Cancion buscarPorTitulo(String titulo) {
        Nodo temp = cabeza;
        while (temp != null) {
            if (temp.cancion.getTitulo().equalsIgnoreCase(titulo)) {
                return temp.cancion;
            }
            temp = temp.siguiente;
        }
        return null;
    }

    public void imprimirLista() {
        Nodo temp = cabeza;
        System.out.println("🎵 Lista de canciones:");
        while (temp != null) {
            System.out.println(" - " + temp.cancion.getTitulo() + " | " + temp.cancion.getArtista());
            temp = temp.siguiente;
        }
    }

    public Cancion obtenerPorIndice(int indice) {
        if (indice < 0 || indice >= tamaño) {
            throw new IndexOutOfBoundsException("Índice fuera de rango");
        }
        Nodo temp = cabeza;
        int i = 0;
        while (i < indice) {
            temp = temp.siguiente;
            i++;
        }
        return temp.cancion;
    }
}
