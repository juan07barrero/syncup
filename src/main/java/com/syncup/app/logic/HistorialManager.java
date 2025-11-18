package com.syncup.app.logic;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h2>HistorialManager</h2>
 * Gestiona el registro de todas las reproducciones de canciones en el sistema.
 * 
 * <p>
 * Mantiene un historial global en memoria y lo persiste en un archivo CSV.
 * El historial registra: usuario, fecha/hora, título de canción y género.
 * </p>
 * 
 * <h3>Funcionalidades:</h3>
 * <ul>
 *     <li>Registrar reproducciones de canciones</li>
 *     <li>Obtener historial por usuario o global</li>
 *     <li>Calcular estadísticas (canción/género más reproducido)</li>
 *     <li>Exportar historial en archivos CSV separados por usuario</li>
 *     <li>Persistencia automática en disco</li>
 * </ul>
 * 
 * <h3>Formato del CSV:</h3>
 * <pre>
 *     usuario,fecha,titulo,genero
 *     juan,2025-11-18 14:30:45,Shape of You,Pop
 * </pre>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class HistorialManager {

    /** Ruta del archivo de historial global */
    private static final String HISTORIAL_PATH = "src/main/resources/data/historial.csv";
    /** Formato para mostrar fecha y hora */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Cada registro: [usuario, fecha, titulo, genero]
    /** Almacenamiento en memoria de todos los registros de historial */
    private final List<String[]> historial = new ArrayList<>();

    /**
     * Constructor por defecto.
     * Inicializa el archivo de historial si no existe y carga todos los registros.
     */
    public HistorialManager() {
        inicializarArchivo();
        cargarHistorialCSV();
    }

    /**
     * Inicializa el archivo de historial en disco si no existe.
     */
    private void inicializarArchivo() {
        try {
            Path path = Paths.get(HISTORIAL_PATH);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                    writer.write("usuario,fecha,titulo,genero");
                    writer.newLine();
                }
                System.out.println("✅ Archivo historial.csv creado correctamente.");
            }
        } catch (IOException e) {
            System.out.println("❌ Error al crear historial.csv: " + e.getMessage());
        }
    }

    /**
     * Carga todos los registros del historial desde el archivo CSV.
     */
    public void cargarHistorialCSV() {
        historial.clear();
        Path path = Paths.get(HISTORIAL_PATH);
        if (!Files.exists(path))
            return;

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            reader.readLine(); // cabecera
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",", 4);
                if (partes.length == 4) {
                    historial.add(new String[] { partes[0], partes[1], partes[2], partes[3] });
                }
            }
            System.out.println("📜 Historial cargado: " + historial.size() + " registros.");
        } catch (IOException e) {
            System.out.println("❌ Error leyendo historial CSV: " + e.getMessage());
        }
    }

    /**
     * Guarda todos los registros del historial en el archivo CSV.
     */
    public void guardarHistorialCSV() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(HISTORIAL_PATH),
                StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            writer.write("usuario,fecha,titulo,genero");
            writer.newLine();
            for (String[] r : historial) {
                writer.write(String.join(",", r));
                writer.newLine();
            }
            System.out.println("💾 Historial guardado correctamente.");
        } catch (IOException e) {
            System.out.println("❌ Error al guardar historial: " + e.getMessage());
        }
    }

    /**
     * Registra una reproducción de canción en el historial.
     * Se obtiene el usuario activo de DataStore, o "Invitado" si no hay sesión.
     * 
     * @param titulo título de la canción reproducida
     * @param genero género de la canción
     */
    public void registrarReproduccion(String titulo, String genero) {
        String usuario = "Invitado";
        DataStore ds = DataStore.getInstance();
        if (ds != null) {
            String activo = ds.getUsuarioActivo();
            if (activo != null && !activo.isEmpty()) {
                usuario = activo;
            }
        }

        String fecha = LocalDateTime.now().format(FORMATTER);
        String[] registro = { usuario, fecha, limpiar(titulo), limpiar(genero) };
        historial.add(registro);

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(HISTORIAL_PATH),
                StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            writer.write(String.join(",", registro));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("❌ Error al escribir historial: " + e.getMessage());
        }

        System.out.println("🎧 Registro agregado: [" + usuario + "] " + titulo);
    }

    /**
     * Limpia una cadena de texto eliminando comas y saltos de línea.
     * 
     * @param texto texto a limpiar
     * @return texto limpio
     */
    private String limpiar(String texto) {
        if (texto == null)
            return "";
        return texto.replace(",", " ").replace("\n", " ").trim();
    }

    // =====================================================
    // MÉTODOS DE CONSULTA GLOBAL Y POR USUARIO
    // =====================================================

    /**
     * Obtiene el historial completo (todos los registros).
     * 
     * @return lista de registros del historial
     */
    public List<String[]> obtenerHistorial() {
        return new ArrayList<>(historial);
    }

    /**
     * Obtiene solo los registros de reproducciones de un usuario específico.
     * 
     * @param usuario nombre de usuario
     * @return lista de registros del usuario, lista vacía si no hay registros
     */
    public List<String[]> obtenerHistorialUsuario(String usuario) {
        if (usuario == null || usuario.isEmpty())
            return Collections.emptyList();
        return historial.stream()
                .filter(r -> r[0].equalsIgnoreCase(usuario))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el historial agrupado por usuario.
     * 
     * @return mapa usuario → lista de registros del usuario
     */
    public Map<String, List<String[]>> obtenerHistorialAgrupadoPorUsuario() {
        return historial.stream()
                .collect(Collectors.groupingBy(r -> r[0]));
    }

    /**
     * Devuelve el número total de reproducciones registradas.
     * 
     * @return cantidad de reproducciones
     */
    public int obtenerTotalReproducciones() {
        return historial.size();
    }

    /**
     * Obtiene la canción más reproducida en todo el sistema.
     * 
     * @return título de la canción más reproducida
     */
    public String obtenerCancionMasReproducida() {
        if (historial.isEmpty())
            return "Sin datos";
        return historial.stream()
                .collect(Collectors.groupingBy(r -> r[2], Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Sin datos");
    }

    /**
     * Obtiene el género más reproducido en todo el sistema.
     * 
     * @return género más reproducido
     */
    public String obtenerGeneroMasReproducido() {
        if (historial.isEmpty())
            return "Sin datos";
        return historial.stream()
                .collect(Collectors.groupingBy(r -> r[3], Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Sin datos");
    }

    /**
     * Calcula el promedio de reproducciones por día.
     * 
     * @return promedio de reproducciones diarias
     */
    public double obtenerPromedioReproduccionesPorDia() {
        if (historial.isEmpty())
            return 0.0;
        Map<String, Long> porDia = historial.stream()
                .map(r -> r[1].split(" ")[0])
                .collect(Collectors.groupingBy(f -> f, Collectors.counting()));
        return (double) historial.size() / porDia.size();
    }

    // =====================================================
    // MÉTRICAS POR USUARIO
    // =====================================================

    /**
     * Obtiene la canción más reproducida por un usuario específico.
     * 
     * @param usuario nombre de usuario
     * @return título de la canción más reproducida por el usuario
     */
    public String obtenerCancionMasReproducidaUsuario(String usuario) {
        List<String[]> lista = obtenerHistorialUsuario(usuario);
        if (lista.isEmpty())
            return "Sin datos";
        return lista.stream()
                .collect(Collectors.groupingBy(r -> r[2], Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Sin datos");
    }

    /**
     * Obtiene el género más reproducido por un usuario específico.
     * 
     * @param usuario nombre de usuario
     * @return género más reproducido por el usuario
     */
    public String obtenerGeneroMasReproducidoUsuario(String usuario) {
        List<String[]> lista = obtenerHistorialUsuario(usuario);
        if (lista.isEmpty())
            return "Sin datos";
        return lista.stream()
                .collect(Collectors.groupingBy(r -> r[3], Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Sin datos");
    }

    /**
     * Cuenta las reproducciones por canción en todo el historial.
     * 
     * @return mapa canción → cantidad de reproducciones
     */
    public Map<String, Long> conteoPorCancion() {
        return historial.stream()
                .collect(Collectors.groupingBy(r -> r[2], Collectors.counting()));
    }

    /**
     * Cuenta las reproducciones por género en todo el historial.
     * 
     * @return mapa género → cantidad de reproducciones
     */
    public Map<String, Long> conteoPorGenero() {
        return historial.stream()
                .collect(Collectors.groupingBy(r -> r[3], Collectors.counting()));
    }

    /**
     * Elimina todos los registros de un usuario específico del historial.
     * 
     * @param usuario nombre de usuario
     */
    public void eliminarHistorialUsuario(String usuario) {
        historial.removeIf(r -> r[0].equalsIgnoreCase(usuario));
        guardarHistorialCSV();
    }

    /**
     * Limpia completamente el historial global.
     */
    public void limpiarHistorialGlobal() {
        historial.clear();
        guardarHistorialCSV();
    }

    /**
     * Exporta el historial completo agrupado por usuario en archivos CSV separados.
     * Crea la carpeta /exports si no existe.
     */
    public void exportarHistorialPorUsuario() {
        Map<String, List<String[]>> agrupado = obtenerHistorialAgrupadoPorUsuario();

        Path carpeta = Paths.get("src/main/resources/exports/");
        try {
            if (!Files.exists(carpeta)) {
                Files.createDirectories(carpeta);
            }

            for (Map.Entry<String, List<String[]>> entry : agrupado.entrySet()) {
                String usuario = entry.getKey();
                List<String[]> registros = entry.getValue();

                Path archivo = carpeta.resolve("historial_" + usuario + ".csv");
                try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
                    writer.write("usuario,fecha,titulo,genero");
                    writer.newLine();
                    for (String[] r : registros) {
                        writer.write(String.join(",", r));
                        writer.newLine();
                    }
                }
            }

            System.out.println("📤 Exportación completada. Archivos generados en /exports/");
        } catch (IOException e) {
            System.out.println("❌ Error exportando historial: " + e.getMessage());
        }
    }

    /**
     * Devuelve un mapa de reproducciones agrupadas por género.
     * 
     * @return mapa género → cantidad de reproducciones
     */
    public Map<String, Integer> getReproduccionesPorGenero() {
        return historial.stream()
                .collect(Collectors.toMap(
                        r -> r[3], // genero
                        r -> 1,
                        Integer::sum));
    }

    /**
     * Devuelve un mapa de reproducciones agrupadas por canción.
     * 
     * @return mapa canción → cantidad de reproducciones
     */
    public Map<String, Integer> getReproduccionesPorCancion() {
        return historial.stream()
                .collect(Collectors.toMap(
                        r -> r[2], // título
                        r -> 1,
                        Integer::sum));
    }

    /**
     * Devuelve un mapa de reproducciones agrupadas por artista.
     * 
     * @return mapa artista → cantidad de reproducciones
     */
    public Map<String, Integer> getReproduccionesPorArtista() {
        return historial.stream()
                .map(r -> r[2]) // título
                .map(titulo -> {
                    var cancion = DataStore.getInstance().getBiblioteca().buscarPorTitulo(titulo);
                    return (cancion != null) ? cancion.getArtista() : "Desconocido";
                })
                .collect(Collectors.toMap(
                        artista -> artista,
                        artista -> 1,
                        Integer::sum));
    }

}
