package com.syncup.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.syncup.app.Main;
import com.syncup.app.model.Cancion;
import com.syncup.app.model.BibliotecaMusical;
import com.syncup.app.logic.HistorialManager;
import com.syncup.app.logic.DataStore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/**
 * <h2>Controlador Principal del Usuario</h2>
 * Panel central del sistema SyncUp para usuarios registrados.
 * <p>
 * Coordina todas las funcionalidades de usuario incluyendo:
 * </p>
 * <ul>
 *     <li>Reproducción de música con controles multimedia</li>
 *     <li>Búsqueda y filtrado de canciones por criterios diversos</li>
 *     <li>Gestión de playlists personalizadas</li>
 *     <li>Gestión de canciones favoritas</li>
 *     <li>Interacción social: seguir usuarios y recibir sugerencias</li>
 *     <li>Visualización de recomendaciones personalizadas</li>
 *     <li>Exportación de historial personal</li>
 * </ul>
 * <p>
 * <b>Paneles principales:</b>
 * </p>
 * <ul>
 *     <li><b>panelBiblioteca</b>: Búsqueda y reproducción de canciones</li>
 *     <li><b>panelPlaylists</b>: Gestión de playlists y favoritos</li>
 *     <li><b>panelSocial</b>: Búsqueda de usuarios, sugerencias y recomendaciones</li>
 * </ul>
 * 
 * @author Sistema SyncUp
 * @version 1.0
 */
public class UsuarioController {

    @FXML
    private TableView<Cancion> tablaCanciones;
    @FXML
    private TextField txtBuscar;
    @FXML
    private TableColumn<Cancion, String> colTitulo;
    @FXML
    private TableColumn<Cancion, String> colArtista;
    @FXML
    private TableColumn<Cancion, String> colGenero;
    @FXML
    private TextField txtTituloNuevo;
    @FXML
    private TextField txtArtistaNuevo;
    @FXML
    private TextField txtGeneroNuevo;
    @FXML
    private Slider sliderProgreso;
    @FXML
    private Label lblTiempo;
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnAdminDashboard;
    @FXML
    private Label lblCancionActual;
    @FXML
    private TextField txtNombrePlaylist;
    @FXML
    private StackPane panelPrincipal;
    @FXML
    private AnchorPane panelBiblioteca;
    @FXML
    private AnchorPane panelPlaylists;
    @FXML
    private ListView<String> listaPlaylists;
    @FXML
    private Label lblNowPlaying; // “Reproduciendo ahora”
    // --- Panel social ---
    @FXML
    private AnchorPane panelSocial;
    @FXML
    private TextField txtBuscarUsuario;
    @FXML
    private ListView<String> listaResultadosUsuarios;
    @FXML
    private ListView<String> listaSeguidos;
    @FXML
    private ListView<String> listaSugerencias;
    @FXML
    private ListView<String> listaSugerenciasBusqueda;
    @FXML
    private ListView<String> listaRecomendadas;

    private MediaPlayer mediaPlayer;
    private int indiceActual = -1;
    private boolean arrastrandoSlider = false;
    private boolean radioActiva = false;

    private final BibliotecaMusical biblioteca = DataStore.getInstance().getBiblioteca();
    private ObservableList<Cancion> listaObservable;
    private final HistorialManager historialManager = DataStore.getInstance().getHistorial();

    // Inicialización
    @FXML
    private void initialize() {
        colTitulo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("artista"));
        colGenero.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("genero"));

        cargarCanciones();
        biblioteca.reconstruirTrie();

        sliderProgreso.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (sliderProgreso.isValueChanging() && mediaPlayer != null) {
                arrastrandoSlider = true;
                mediaPlayer.seek(Duration.seconds(newVal.doubleValue()));
            } else {
                arrastrandoSlider = false;
            }
        });

        String rol = DataStore.getInstance().getRolUsuarioActivo();
        if (!"admin".equalsIgnoreCase(rol)) {
            if (btnAdminDashboard != null)
                btnAdminDashboard.setVisible(false);
            if (txtTituloNuevo != null)
                txtTituloNuevo.setDisable(true);
            if (txtArtistaNuevo != null)
                txtArtistaNuevo.setDisable(true);
            if (txtGeneroNuevo != null)
                txtGeneroNuevo.setDisable(true);
            if (btnAgregar != null)
                btnAgregar.setDisable(true);
            if (btnEliminar != null)
                btnEliminar.setDisable(true);
        }

        mostrarBiblioteca(null);
        // Vista por defecto

        // Texto inicial del Now Playing
        if (lblNowPlaying != null)
            lblNowPlaying.setText("—");

        // Panel social inicialmente oculto
        if (panelSocial != null) {
            panelSocial.setVisible(false);
            panelSocial.setManaged(false);
        }

        // 👇 Añade esta línea para cargar los favoritos del usuario activo
        cargarFavoritosUsuario();

        if (lblNowPlaying != null)
            lblNowPlaying.setText("—");

        // AUTOCOMPLETADO (TRIE)
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            manejarAutocompletado(newVal);
        });

        // Ocultar sugerencias cuando se clickea fuera
        if (listaSugerenciasBusqueda != null) {
            listaSugerenciasBusqueda.setOnMouseClicked(e -> {
                String sel = listaSugerenciasBusqueda.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    txtBuscar.setText(sel);
                    listaSugerenciasBusqueda.setVisible(false);
                    listaSugerenciasBusqueda.setManaged(false);
                }
            });
        }

    }

    private void cargarFavoritosUsuario() {
        String usuario = getUsuarioActual();
        var favoritos = DataStore.getInstance().getFavoritos().obtenerFavoritos(usuario);

        if (favoritos != null && !favoritos.isEmpty()) {
            System.out.println("🎵 Favoritos cargados para " + usuario + ": " + favoritos.size());
        } else {
            System.out.println("ℹ️ No se encontraron favoritos para " + usuario);
        }
    }

    // Biblioteca
    private void cargarCanciones() {
        List<Cancion> lista = biblioteca.obtenerTodas();
        listaObservable = FXCollections.observableArrayList(lista);
        tablaCanciones.setItems(listaObservable);
    }

    @FXML
    private void handleBuscar(ActionEvent event) {

        String filtro = txtBuscar.getText().trim().toLowerCase();

        // Si está vacío → mostrar toda la biblioteca
        if (filtro.isEmpty()) {
            tablaCanciones.setItems(listaObservable);
            return;
        }

        // Sugerencias desde el TRIE
        List<String> titulos = biblioteca.autocompletar(filtro);

        ObservableList<Cancion> filtradas = FXCollections.observableArrayList();

        for (Cancion c : biblioteca.obtenerTodas()) {
            if (titulos.contains(c.getTitulo())) { // 🔥 ahora coincide correctamente
                filtradas.add(c);
            }
        }

        tablaCanciones.setItems(filtradas);

        // Ocultar la lista de sugerencias
        listaSugerenciasBusqueda.setVisible(false);
        listaSugerenciasBusqueda.setManaged(false);
    }

    @FXML
    private void handleAgregar(ActionEvent event) {
        String titulo = txtTituloNuevo.getText() != null ? txtTituloNuevo.getText().trim() : "";
        String artista = txtArtistaNuevo.getText() != null ? txtArtistaNuevo.getText().trim() : "";
        String genero = txtGeneroNuevo.getText() != null ? txtGeneroNuevo.getText().trim() : "";

        if (titulo.isEmpty() || artista.isEmpty() || genero.isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios.");
            return;
        }

        Cancion nueva = new Cancion(titulo, artista, genero);
        biblioteca.agregarCancion(nueva);
        cargarCanciones();

        if (txtTituloNuevo != null)
            txtTituloNuevo.clear();
        if (txtArtistaNuevo != null)
            txtArtistaNuevo.clear();
        if (txtGeneroNuevo != null)
            txtGeneroNuevo.clear();

        mostrarAlerta("Éxito", "Canción agregada correctamente.");
    }

    // Reproducción
    @FXML
    private void handleReproducirSeleccionada(ActionEvent event) {

        // 1️⃣ Intentar reproducir desde la tabla principal
        Cancion seleccionada = tablaCanciones.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            indiceActual = listaObservable.indexOf(seleccionada);
            reproducirCancion(seleccionada);
            return;
        }

        // 2️⃣ Si no había nada seleccionado en la tabla, revisar recomendaciones
        String recomendado = listaRecomendadas.getSelectionModel()
                .getSelectedItem();

        if (recomendado != null && !recomendado.contains("Sin recomendaciones")) {

            // esperado: "Titulo — Artista"
            String titulo = recomendado.split(" — ")[0].trim();

            Cancion c = biblioteca.buscarPorTitulo(titulo);
            if (c != null) {
                reproducirCancion(c);
                return;
            }
        }

        // 3️⃣ Si no seleccionaron nada válido
        mostrarAlerta("Atención", "Selecciona una canción para reproducir.");
    }

    private void stopAndDisposeMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING
                        || status == MediaPlayer.Status.PAUSED
                        || status == MediaPlayer.Status.STOPPED) {
                    mediaPlayer.stop();
                }
                mediaPlayer.dispose();
            }
        } catch (Exception e) {
            System.out.println("⚠️ Error al detener mediaPlayer: " + e.getMessage());
        } finally {
            mediaPlayer = null;
        }
    }

    private void reproducirCancion(Cancion cancion) {
        try {
            stopAndDisposeMediaPlayer();

            String archivo = cancion.getTitulo().trim() + ".mp3";
            URL url = getClass().getResource("/audio/" + archivo);

            if (url == null) {
                mostrarAlerta("Archivo no encontrado", "No se encontró el archivo de audio: " + archivo);
                return;
            }

            Media media = new Media(url.toExternalForm());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!arrastrandoSlider)
                    sliderProgreso.setValue(newTime.toSeconds());
                actualizarLabelTiempo(newTime, media.getDuration());
            });

            mediaPlayer.setOnReady(() -> {
                sliderProgreso.setMax(media.getDuration().toSeconds());
                actualizarLabelTiempo(Duration.ZERO, media.getDuration());
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                stopAndDisposeMediaPlayer();
                handleSiguiente(null);
            });

            mediaPlayer.play();

            if (lblNowPlaying != null) {
                lblNowPlaying.setText("🎵 " + cancion.getTitulo() + " — " + cancion.getArtista());
            }
            if (lblCancionActual != null) {
                lblCancionActual.setText(cancion.getTitulo() + " - " + cancion.getArtista());
            }

            // Registrar reproducción en historial (SOLO UNA VEZ)
            historialManager.registrarReproduccion(cancion.getTitulo(), cancion.getGenero());

            // 💡 Actualizar recomendaciones a la derecha
            actualizarRecomendadas(cancion);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo reproducir la canción.");
        }
    }

    private void actualizarLabelTiempo(Duration actual, Duration total) {
        int sa = (int) actual.toSeconds();
        int st = (int) total.toSeconds();
        if (lblTiempo != null) {
            lblTiempo.setText(String.format("%02d:%02d / %02d:%02d",
                    sa / 60, sa % 60, st / 60, st % 60));
        }
    }

    @FXML
    private void mostrarSocial(ActionEvent e) {
        if (panelBiblioteca != null) {
            panelBiblioteca.setVisible(false);
            panelBiblioteca.setManaged(false);
        }
        if (panelPlaylists != null) {
            panelPlaylists.setVisible(false);
            panelPlaylists.setManaged(false);
        }
        if (panelSocial != null) {
            panelSocial.setVisible(true);
            panelSocial.setManaged(true);
        }

        refrescarSocial();
    }

    @FXML
    private void handlePausa(ActionEvent event) {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                mediaPlayer.play();
            }
        }
    }

    @FXML
    private void handleSiguiente(ActionEvent event) {
        if (listaObservable == null || listaObservable.isEmpty())
            return;

        // 👉 Si NO estamos en radio y estamos en la última canción
        // intentamos iniciar la radio automática
        if (!radioActiva && indiceActual >= listaObservable.size() - 1) {

            boolean inicioRadio = iniciarRadioDesdeFin();
            if (inicioRadio) {
                return; // Ya inició radio y empezó a sonar la primera recomendada
            }

            // Si no se pudo generar radio, sigue comportamiento normal (circular)
        }

        // 👉 Avance normal de la cola
        indiceActual = (indiceActual + 1) % listaObservable.size();
        reproducirCancion(listaObservable.get(indiceActual));
    }

    @FXML
    private void handleAnterior(ActionEvent event) {
        if (listaObservable == null || listaObservable.isEmpty())
            return;
        indiceActual = (indiceActual - 1 + listaObservable.size()) % listaObservable.size();
        reproducirCancion(listaObservable.get(indiceActual));
    }

    // Navegación (aplica tema oscuro)
    @FXML
    private void handleAbrirEstadisticas(ActionEvent event) {
        cambiarVista("/views/estadisticas.fxml", "SyncUp - Estadísticas");
    }

    @FXML
    private void handleAbrirEstadisticasAdmin(ActionEvent event) {
        cambiarVista("/views/estadisticas_admin.fxml", "SyncUp - Estadísticas Administrativas");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            stopAndDisposeMediaPlayer();
            historialManager.guardarHistorialCSV();
            cambiarVista("/views/login.fxml", "SyncUp - Inicio de Sesión");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cambiarVista(String ruta, String titulo) {
        try {
            stopAndDisposeMediaPlayer();
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(ruta));
            Scene scene = new Scene(loader.load());
            // aplica tema oscuro a cualquier escena nueva
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
            Stage stage = (Stage) tablaCanciones.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(titulo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // FAVORITOS
    @FXML
    private void handleToggleFavorito(ActionEvent event) {
        Cancion seleccionada = tablaCanciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Favoritos", "Primero selecciona una canción de la tabla.");
            return;
        }
        String usuario = getUsuarioActual();
        boolean ahoraEsFavorita = DataStore.getInstance()
                .getFavoritos()
                .toggleFavorito(usuario, seleccionada);

        mostrarAlerta("Favoritos",
                (ahoraEsFavorita ? "Añadida a favoritos: " : "Eliminada de favoritos: ")
                        + seleccionada.getTitulo());
    }

    @FXML
    private void handleReproducirFavoritos(ActionEvent event) {
        var ds = DataStore.getInstance();
        String usuario = getUsuarioActual();
        var favTitulos = ds.getFavoritos().obtenerFavoritos(usuario); // List<String> de títulos

        if (favTitulos == null || favTitulos.isEmpty()) {
            mostrarAlerta("Favoritos", "Aún no tienes canciones en favoritos.");
            return;
        }

        // Mapear a Cancion usando la biblioteca
        var todas = biblioteca.obtenerTodas();
        var cola = FXCollections.<Cancion>observableArrayList();
        for (Cancion fav : favTitulos) {
            for (Cancion c : todas) {
                if (c.getTitulo().equalsIgnoreCase(fav.getTitulo())) {
                    cola.add(c);
                    break;
                }
            }
        }
        if (cola.isEmpty()) {
            mostrarAlerta("Favoritos", "No se encontraron esas canciones en la biblioteca.");
            return;
        }
        radioActiva = false;

        listaObservable = cola;
        indiceActual = 0;
        tablaCanciones.setItems(listaObservable);
        reproducirCancion(listaObservable.get(indiceActual));
        mostrarBiblioteca(null);
    }

    private String getUsuarioActual() {
        String u = DataStore.getInstance().getUsuarioActivo();
        return (u != null && !u.isEmpty()) ? u : "Invitado";
    }

    // PLAYLISTS - navegación vistas
    @FXML
    private void mostrarBiblioteca(ActionEvent e) {
        if (panelBiblioteca != null) {
            panelBiblioteca.setVisible(true);
            panelBiblioteca.setManaged(true);
        }
        if (panelPlaylists != null) {
            panelPlaylists.setVisible(false);
            panelPlaylists.setManaged(false);
        }
        if (panelSocial != null) {
            panelSocial.setVisible(false);
            panelSocial.setManaged(false);
        }
    }

    // PLAYLISTS - operaciones
    @FXML
    private void handleCrearPlaylist(ActionEvent event) {

        System.out.println("DEBUG txtNombrePlaylist = " + txtNombrePlaylist);
        System.out.println("DEBUG txtNombrePlaylist.getText() = " +
                (txtNombrePlaylist != null ? txtNombrePlaylist.getText() : "NULL"));

        String nombre = (txtNombrePlaylist != null && txtNombrePlaylist.getText() != null)
                ? txtNombrePlaylist.getText().trim()
                : "";

        if (nombre.isEmpty()) {
            mostrarAlerta("Playlists", "Escribe un nombre para la playlist.");
            return;
        }

        var ds = DataStore.getInstance();
        boolean ok = ds.getPlaylists().crearPlaylist(getUsuarioActual(), nombre);
        if (ok) {
            txtNombrePlaylist.clear();
            recargarListaPlaylists();
            mostrarAlerta("Playlists", "Playlist creada: " + nombre);
        } else {
            mostrarAlerta("Playlists", "Ya existe una playlist con ese nombre.");
        }
    }

    @FXML
    private void mostrarPlaylists(ActionEvent e) {

        if (panelBiblioteca != null) {
            panelBiblioteca.setVisible(false);
            panelBiblioteca.setManaged(false);
        }
        if (panelSocial != null) {
            panelSocial.setVisible(false);
            panelSocial.setManaged(false);
        }
        if (panelPlaylists != null) {
            panelPlaylists.setVisible(true);
            panelPlaylists.setManaged(true);
        }

        recargarListaPlaylists();
    }

    @FXML
    private void handleReproducirPlaylist(ActionEvent event) {
        if (listaPlaylists == null) {
            mostrarAlerta("Playlists", "No hay lista de playlists en la vista.");
            return;
        }
        String sel = listaPlaylists.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Playlists", "Selecciona una playlist primero.");
            return;
        }

        var ds = DataStore.getInstance();
        var titulos = ds.getPlaylists().obtenerTitulosDePlaylist(getUsuarioActual(), sel);

        if (titulos == null || titulos.isEmpty()) {
            mostrarAlerta("Playlists", "La playlist está vacía.");
            return;
        }

        var todas = biblioteca.obtenerTodas();
        var cola = FXCollections.<Cancion>observableArrayList();
        for (String t : titulos) {
            for (Cancion c : todas) {
                if (c.getTitulo().equalsIgnoreCase(t)) {
                    cola.add(c);
                    break;
                }
            }
        }
        if (cola.isEmpty()) {
            mostrarAlerta("Playlists", "No se encontraron coincidencias en la biblioteca.");
            return;
        }
        radioActiva = false;
        listaObservable = cola;
        indiceActual = 0;
        tablaCanciones.setItems(listaObservable);
        reproducirCancion(listaObservable.get(indiceActual));
        mostrarBiblioteca(null);
    }

    /**
     * Genera una lista de canciones recomendadas tipo "radio"
     * usando favoritos + historial (género / canción más escuchada).
     */
    private ObservableList<Cancion> generarRadioPersonalizada() {
        List<Cancion> todas = biblioteca.obtenerTodas();
        if (todas == null || todas.isEmpty()) {
            return FXCollections.observableArrayList();
        }

        var ds = DataStore.getInstance();
        String usuario = getUsuarioActual();
        var favoritos = ds.getFavoritos().obtenerFavoritos(usuario); // List<Cancion>

        Set<String> generosFav = new HashSet<>();
        Set<String> artistasFav = new HashSet<>();
        Set<String> titulosFav = new HashSet<>();

        if (favoritos != null) {
            for (Cancion fav : favoritos) {
                if (fav.getGenero() != null)
                    generosFav.add(fav.getGenero().toLowerCase());
                if (fav.getArtista() != null)
                    artistasFav.add(fav.getArtista().toLowerCase());
                if (fav.getTitulo() != null)
                    titulosFav.add(fav.getTitulo().toLowerCase());
            }
        }

        String generoTopHist = historialManager.obtenerGeneroMasReproducido();
        if (generoTopHist != null)
            generoTopHist = generoTopHist.toLowerCase();

        String tituloTopHist = historialManager.obtenerCancionMasReproducida();
        if (tituloTopHist != null)
            tituloTopHist = tituloTopHist.toLowerCase();

        Map<Cancion, Integer> puntajes = new HashMap<>();

        for (Cancion c : todas) {
            int score = 0;
            String t = c.getTitulo() != null ? c.getTitulo().toLowerCase() : "";
            String a = c.getArtista() != null ? c.getArtista().toLowerCase() : "";
            String g = c.getGenero() != null ? c.getGenero().toLowerCase() : "";

            // muy fuerte si está en favoritos
            if (titulosFav.contains(t))
                score += 4;
            if (artistasFav.contains(a))
                score += 3;
            if (generosFav.contains(g))
                score += 2;

            // refuerzo con historial
            if (generoTopHist != null && g.equals(generoTopHist))
                score += 2;
            if (tituloTopHist != null && t.contains(tituloTopHist))
                score += 2;

            if (score > 0) {
                puntajes.put(c, score);
            }
        }

        // Si no hay suficiente info, devolvemos todas mezcladas
        if (puntajes.isEmpty()) {
            List<Cancion> copia = new ArrayList<>(todas);
            Collections.shuffle(copia);
            return FXCollections.observableArrayList(copia);
        }

        List<Cancion> ordenadas = new ArrayList<>(puntajes.keySet());
        ordenadas.sort((c1, c2) -> Integer.compare(
                puntajes.get(c2),
                puntajes.get(c1)));

        // Evitar que la primera canción de la radio sea la misma que está sonando ahora
        if (listaObservable != null &&
                indiceActual >= 0 &&
                indiceActual < listaObservable.size()) {
            Cancion actual = listaObservable.get(indiceActual);
            ordenadas.removeIf(c -> c.getTitulo().equalsIgnoreCase(actual.getTitulo()) &&
                    c.getArtista().equalsIgnoreCase(actual.getArtista()));
        }

        // Limitar tamaño para que no sea infinita
        if (ordenadas.size() > 30) {
            ordenadas = ordenadas.subList(0, 30);
        }

        return FXCollections.observableArrayList(ordenadas);
    }

    /**
     * Inicia la radio automáticamente cuando se llega al final
     * de la cola "normal" o playlist.
     */
    private boolean iniciarRadioDesdeFin() {
        ObservableList<Cancion> recomendadas = generarRadioPersonalizada();
        if (recomendadas == null || recomendadas.isEmpty()) {
            return false;
        }

        radioActiva = true;
        listaObservable = recomendadas;
        indiceActual = 0;
        tablaCanciones.setItems(listaObservable);
        reproducirCancion(listaObservable.get(indiceActual));

        mostrarAlerta("Radio", "Se ha iniciado una radio basada en tus gustos 🎧");
        return true;
    }

    @FXML
    private void handleIniciarRadio(ActionEvent event) {
        ObservableList<Cancion> recomendadas = generarRadioPersonalizada();
        if (recomendadas == null || recomendadas.isEmpty()) {
            mostrarAlerta("Radio", "Aún no hay suficientes datos para generar una radio.");
            return;
        }

        radioActiva = true;
        listaObservable = recomendadas;
        indiceActual = 0;
        tablaCanciones.setItems(listaObservable);
        reproducirCancion(listaObservable.get(indiceActual));
    }

    @FXML
    private void handleEliminarPlaylist(ActionEvent event) {
        if (listaPlaylists == null)
            return;
        String sel = listaPlaylists.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Playlists", "Selecciona una playlist para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar playlist");
        confirm.setHeaderText("¿Eliminar \"" + sel + "\"?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                var ds = DataStore.getInstance();
                boolean ok = ds.getPlaylists().eliminarPlaylist(getUsuarioActual(), sel);
                if (ok) {
                    recargarListaPlaylists();
                    mostrarAlerta("Playlists", "Playlist eliminada: " + sel);
                } else {
                    mostrarAlerta("Playlists", "No se pudo eliminar la playlist.");
                }
            }
        });
    }

    @FXML
    private void handleAgregarAPlaylist(ActionEvent event) {
        Cancion sel = tablaCanciones.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Playlists", "Selecciona una canción de la tabla.");
            return;
        }

        // Sugerir última escrita o pedir nombre
        String nombre = (txtNombrePlaylist != null && txtNombrePlaylist.getText() != null)
                ? txtNombrePlaylist.getText().trim()
                : "";

        if (nombre.isEmpty()) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Agregar a playlist");
            dialog.setHeaderText("¿A qué playlist deseas agregarla?");
            dialog.setContentText("Nombre de playlist:");
            nombre = dialog.showAndWait().orElse("").trim();
            if (nombre.isEmpty()) {
                mostrarAlerta("Playlists", "Debes indicar el nombre de la playlist.");
                return;
            }
        }

        var ds = DataStore.getInstance();
        ds.getPlaylists().crearPlaylist(getUsuarioActual(), nombre); // idempotente
        boolean ok = ds.getPlaylists().agregarCancion(getUsuarioActual(), nombre, sel.getTitulo());

        if (ok) {
            recargarListaPlaylists();
            mostrarAlerta("Playlists", "Se agregó \"" + sel.getTitulo() + "\" a \"" + nombre + "\".");
        } else {
            mostrarAlerta("Playlists", "No se pudo agregar la canción (ya está o hubo un problema).");
        }
    }

    // ======== util ========
    private void recargarListaPlaylists() {
        try {
            var ds = DataStore.getInstance();
            var names = ds.getPlaylists().listarPlaylists(getUsuarioActual());
            if (listaPlaylists != null)
                listaPlaylists.getItems().setAll(names);
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo recargar la lista de playlists: " + e.getMessage());
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        Cancion seleccionada = tablaCanciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Atención", "Selecciona una canción para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Deseas eliminar \"" + seleccionada.getTitulo() + "\" de tu biblioteca?");

        confirm.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                biblioteca.eliminarCancion(seleccionada);
                cargarCanciones();
                mostrarAlerta("Éxito", "Canción eliminada correctamente.");
            }
        });
    }

    // BLOQUE SOCIAL (SEGUIR)

    private void refrescarSocial() {
        String actual = getUsuarioActual();
        var ds = DataStore.getInstance();
        var grafo = ds.getGrafoSocial();

        // Lista de seguidos
        if (listaSeguidos != null) {
            listaSeguidos.getItems().setAll(grafo.obtenerSeguidos(actual));
        }

        // Sugerencias (máx 10)
        if (listaSugerencias != null) {
            var sugerencias = grafo.sugerirUsuarios(actual, 10);
            listaSugerencias.getItems().setAll(sugerencias);
        }
    }

    @FXML
    private void handleBuscarUsuario(ActionEvent event) {
        if (txtBuscarUsuario == null || listaResultadosUsuarios == null)
            return;

        String filtro = txtBuscarUsuario.getText() != null
                ? txtBuscarUsuario.getText().trim().toLowerCase()
                : "";

        listaResultadosUsuarios.getItems().clear();
        if (filtro.isEmpty()) {
            mostrarAlerta("Usuarios", "Escribe algo para buscar.");
            return;
        }

        var ds = DataStore.getInstance();
        var usuarios = ds.obtenerUsuarios(); // List<Usuario>

        for (var u : usuarios) {
            String username = u.getUsername();
            String nombre = u.getNombre() != null ? u.getNombre() : "";
            if (username.equalsIgnoreCase(getUsuarioActual()))
                continue; // no sugerirse a sí mismo

            String combinacion = username + " - " + nombre;
            if (username.toLowerCase().contains(filtro) || nombre.toLowerCase().contains(filtro)) {
                listaResultadosUsuarios.getItems().add(combinacion);
            }
        }

        if (listaResultadosUsuarios.getItems().isEmpty()) {
            mostrarAlerta("Usuarios", "No se encontraron usuarios con ese criterio.");
        }
    }

    private String extraerUsernameDeItem(String item) {
        if (item == null)
            return null;
        int idx = item.indexOf(" - ");
        if (idx > 0) {
            return item.substring(0, idx).trim();
        }
        return item.trim();
    }

    @FXML
    private void handleSeguirUsuario(ActionEvent event) {
        var ds = DataStore.getInstance();
        var grafo = ds.getGrafoSocial();
        String actual = getUsuarioActual();

        String seleccionado = null;

        if (listaResultadosUsuarios != null && !listaResultadosUsuarios.getSelectionModel().isEmpty()) {
            seleccionado = listaResultadosUsuarios.getSelectionModel().getSelectedItem();
        } else if (listaSugerencias != null && !listaSugerencias.getSelectionModel().isEmpty()) {
            seleccionado = listaSugerencias.getSelectionModel().getSelectedItem();
        }

        if (seleccionado == null) {
            mostrarAlerta("Social", "Selecciona un usuario de resultados o sugerencias.");
            return;
        }

        String usernameObjetivo = extraerUsernameDeItem(seleccionado);
        if (usernameObjetivo.equalsIgnoreCase(actual)) {
            mostrarAlerta("Social", "No puedes seguirte a ti mismo.");
            return;
        }

        boolean ok = grafo.seguir(actual, usernameObjetivo);
        if (ok) {
            mostrarAlerta("Social", "Ahora sigues a: " + usernameObjetivo);
            refrescarSocial();
        } else {
            mostrarAlerta("Social", "Ya sigues a " + usernameObjetivo + " o hubo un problema.");
        }
    }

    @FXML
    private void handleDejarSeguirUsuario(ActionEvent event) {
        var ds = DataStore.getInstance();
        var grafo = ds.getGrafoSocial();
        String actual = getUsuarioActual();

        if (listaSeguidos == null) {
            mostrarAlerta("Social", "No hay lista de seguidos en la vista.");
            return;
        }

        String seleccionado = listaSeguidos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Social", "Selecciona un usuario que ya sigues.");
            return;
        }

        String usernameObjetivo = extraerUsernameDeItem(seleccionado); // por si algún día mostramos "user - nombre"
        boolean ok = grafo.dejarDeSeguir(actual, usernameObjetivo);
        if (ok) {
            mostrarAlerta("Social", "Has dejado de seguir a: " + usernameObjetivo);
            refrescarSocial();
        } else {
            mostrarAlerta("Social", "No se pudo dejar de seguir a " + usernameObjetivo + ".");
        }
    }

    private void manejarAutocompletado(String prefijo) {
        if (listaSugerenciasBusqueda == null)
            return;

        if (prefijo == null || prefijo.trim().isEmpty()) {
            listaSugerenciasBusqueda.setVisible(false);
            listaSugerenciasBusqueda.setManaged(false);
            return;
        }

        String filtro = prefijo.trim().toLowerCase();
        var sugerencias = FXCollections.<String>observableArrayList();

        // Por ahora usamos la biblioteca directamente; luego se puede enchufar al TRIE.
        for (Cancion c : biblioteca.obtenerTodas()) {
            String titulo = c.getTitulo() != null ? c.getTitulo() : "";
            if (titulo.toLowerCase().startsWith(filtro)) {
                sugerencias.add(titulo);
            }
        }

        if (sugerencias.isEmpty()) {
            listaSugerenciasBusqueda.setVisible(false);
            listaSugerenciasBusqueda.setManaged(false);
        } else {
            listaSugerenciasBusqueda.setItems(sugerencias);
            listaSugerenciasBusqueda.setVisible(true);
            listaSugerenciasBusqueda.setManaged(true);
        }
    }

    @FXML
    private void handleActualizarSocial(ActionEvent event) {
        refrescarSocial();
        mostrarAlerta("Social", "Conexiones y sugerencias actualizadas.");
    }

    @FXML
    private void handleRadioRecomendada(ActionEvent event) {
        String usuario = getUsuarioActual();
        var ds = DataStore.getInstance();

        var recomendaciones = ds.getRecomendador()
                .recomendarParaUsuario(usuario, 20); // por ejemplo 20 canciones

        if (recomendaciones == null || recomendaciones.isEmpty()) {
            mostrarAlerta("Radio recomendada",
                    "No se pudieron generar recomendaciones.\n" +
                            "Escucha y marca algunas canciones como favoritas primero.");
            return;
        }

        // Cargamos la "radio" en la cola de reproducción
        listaObservable = FXCollections.observableArrayList(recomendaciones);
        indiceActual = 0;
        tablaCanciones.setItems(listaObservable);
        reproducirCancion(listaObservable.get(indiceActual));

        // nos aseguramos de estar en la vista de biblioteca
        mostrarBiblioteca(null);
    }

    // RECOMENDACIONES (BK-TREE)

    private void actualizarRecomendadas(Cancion base) {
        if (listaRecomendadas == null || base == null)
            return;

        // Distancia máx 6, hasta 15 resultados
        var recomendadas = biblioteca.recomendarSimilares(base, 6, 15);

        listaRecomendadas.getItems().clear();

        for (Cancion c : recomendadas) {
            String texto = c.getTitulo() + " — " + c.getArtista();
            listaRecomendadas.getItems().add(texto);
        }

        if (listaRecomendadas.getItems().isEmpty()) {
            listaRecomendadas.getItems().add("Sin recomendaciones similares 😢");
        }
    }

    @FXML
    private void abrirEditarPerfil(ActionEvent event) {
        try {
            // Detener audio
            stopAndDisposeMediaPlayer();

            // 2Cargar FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/editarPerfil.fxml"));
            Parent root = loader.load();

            //  Inicializar controlador con usuario actual y manager
            EditarPerfilController controller = loader.getController();
            controller.inicializar(
                    DataStore.getInstance().getUsuarioActivoObj(),
                    DataStore.getInstance().getUsuarioManager());

            //  Crear la escena
            Scene scene = new Scene(root);

            // ⭐⭐ --- PASO 3 VA AQUÍ --- ⭐⭐
            // Aplicamos tu dark-theme a ESTA nueva ventana
            scene.getStylesheets().add(
                    getClass().getResource("/styles/dark-theme.css").toExternalForm());

            // Mostrar ventana como modal
            Stage stage = new Stage();
            stage.setTitle("Editar Perfil");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
