package com.syncup.app.model.estructuras;

import com.syncup.app.model.Cancion;
import com.syncup.app.model.estructuras.ListaCanciones;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que gestiona la biblioteca musical completa del sistema.
 * Controla la carga desde CSV, búsqueda, agregado y eliminación.
 */
public class BibliotecaMusical {

    private final ListaCanciones canciones;
    private final String CSV_PATH = "src/main/resources/data/canciones.csv";

    public BibliotecaMusical() {
        this.canciones = new ListaCanciones();
        cargarDesdeCSV();
    }

    /**
     * Carga todas las canciones desde el archivo CSV al iniciar.
     */
    private void cargarDesdeCSV() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(CSV_PATH), StandardCharsets.UTF_8))) {

            String linea;
            reader.readLine(); // Saltar encabezado

            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length == 3) {
                    Cancion c = new Cancion(partes[0].trim(), partes[1].trim(), partes[2].trim());
                    canciones.agregarAlFinal(c);
                }
            }

            System.out.println("✅ Biblioteca cargada correctamente. Total: " + canciones.getTamaño());

        } catch (IOException e) {
            System.out.println("⚠️ No se pudo cargar el CSV: " + e.getMessage());
        }
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
     * Agrega una nueva canción.
     */
    public void agregarCancion(Cancion c) {
        canciones.agregarAlFinal(c);
        guardarEnCSV();
    }

    /**
     * Elimina una canción por título.
     */
    public void eliminarCancion(String titulo) {
        canciones.eliminarPorTitulo(titulo);
        guardarEnCSV();
    }

    /**
     * Busca una canción por título (búsqueda secuencial).
     */
    public Cancion buscarPorTitulo(String titulo) {
        return canciones.buscarPorTitulo(titulo);
    }

    /**
     * Retorna todas las canciones como lista (para usar en TableView).
     */
    public List<Cancion> obtenerTodas() {
        List<Cancion> lista = new ArrayList<>();
        for (int i = 0; i < canciones.getTamaño(); i++) {
            lista.add(canciones.obtenerPorIndice(i));
        }
        return lista;
    }

    /**
     * Imprime la lista completa de canciones en consola.
     */
    public void imprimirBiblioteca() {
        canciones.imprimirLista();
    }
}
