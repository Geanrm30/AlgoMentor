package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ni.edu.uam.algomentor.model.Algoritmo;
import ni.edu.uam.algomentor.model.Ejercicio;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.service.ResumenAdministracionService;
import ni.edu.uam.algomentor.util.Actualizable;
import ni.edu.uam.algomentor.util.MenuContextual;
import ni.edu.uam.algomentor.util.SesionUsuario;
import ni.edu.uam.algomentor.util.TextoInterfaz;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResumenController implements Actualizable {

    private static final Logger LOG = Logger.getLogger(ResumenController.class.getName());
    private static final List<String> NOMBRES_GRAFICOS = List.of(
            "Ejercicios por algoritmo (circular)",
            "Ejercicios por dificultad (barras)",
            "Ejercicios por origen (circular)");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Huella de los últimos datos mostrados; evita redibujar (y perder la selección) si nada cambió. */
    private int huellaMostrada;
    private boolean hayDatosMostrados;
    private boolean cargando;
    private List<Perfil> perfilesCargados = List.of();
    private Consumer<Perfil> alVerProgreso = perfil -> { };
    private Consumer<Perfil> alExportarInforme = perfil -> { };

    @FXML private Label lblCantidadAlgoritmos;
    @FXML private Label lblCantidadEjercicios;
    @FXML private Label lblCantidadPerfiles;
    @FXML private Label lblCantidadIntentos;
    @FXML private Label lblEstado;
    @FXML private Button btnActualizar;
    @FXML private Button btnVerProgresoPerfil;
    @FXML private Button btnExportarPerfil;
    @FXML private TableView<ResumenAdministracionService.PuestoRanking> tablaRanking;
    @FXML private TableColumn<ResumenAdministracionService.PuestoRanking, String> colRankPuesto;
    @FXML private TableColumn<ResumenAdministracionService.PuestoRanking, String> colRankPerfil;
    @FXML private TableColumn<ResumenAdministracionService.PuestoRanking, String> colRankIntentos;
    @FXML private TableColumn<ResumenAdministracionService.PuestoRanking, String> colRankCorrectos;
    @FXML private TableColumn<ResumenAdministracionService.PuestoRanking, String> colRankIncorrectos;
    @FXML private TableColumn<ResumenAdministracionService.PuestoRanking, String> colRankPorcentaje;
    @FXML private PieChart graficoEjerciciosAlgoritmo;
    @FXML private ComboBox<String> cmbGrafico;
    @FXML private PieChart graficoEjerciciosOrigen;
    @FXML private BarChart<String, Number> graficoEjerciciosDificultad;
    @FXML private CategoryAxis axisDificultad;
    @FXML private NumberAxis axisCantidadEjercicios;
    @FXML private TableView<Algoritmo> tablaAlgoritmos;
    @FXML private TableColumn<Algoritmo, String> colAlgoritmoId;
    @FXML private TableColumn<Algoritmo, String> colAlgoritmoNombre;
    @FXML private TableColumn<Algoritmo, String> colAlgoritmoCategoria;
    @FXML private TableColumn<Algoritmo, String> colAlgoritmoComplejidad;
    @FXML private TableView<Ejercicio> tablaEjercicios;
    @FXML private TableColumn<Ejercicio, String> colEjercicioId;
    @FXML private TableColumn<Ejercicio, String> colEjercicioAlgoritmo;
    @FXML private TableColumn<Ejercicio, String> colEjercicioDificultad;
    @FXML private TableColumn<Ejercicio, String> colEjercicioOrigen;
    @FXML private TableColumn<Ejercicio, String> colEjercicioDatos;
    @FXML private TableView<Perfil> tablaPerfiles;
    @FXML private TableColumn<Perfil, String> colPerfilId;
    @FXML private TableColumn<Perfil, String> colPerfilNombre;
    @FXML private TableColumn<Perfil, String> colPerfilCreacion;

    /** MainApp decide cómo mostrar el progreso de otro perfil; este controlador solo avisa cuál se eligió. */
    public void setAlVerProgreso(Consumer<Perfil> accion) {
        alVerProgreso = accion == null ? perfil -> { } : accion;
    }

    public void setAlExportarInforme(Consumer<Perfil> accion) {
        alExportarInforme = accion == null ? perfil -> { } : accion;
    }

    /** Clic derecho: acciones sobre el perfil elegido y copiar la fila. */
    private void configurarMenusContextuales() {
        MenuContextual.instalar(tablaPerfiles,
                MenuContextual.sobreSeleccion(tablaPerfiles, "Ver progreso", this::verProgresoPerfil),
                MenuContextual.sobreSeleccion(tablaPerfiles, "Exportar informe PDF…", this::exportarInformePerfil));
        MenuContextual.instalar(tablaRanking,
                MenuContextual.sobreSeleccion(tablaRanking, "Ver progreso",
                        () -> perfilDelRanking().ifPresent(alVerProgreso)),
                MenuContextual.sobreSeleccion(tablaRanking, "Exportar informe PDF…",
                        () -> perfilDelRanking().ifPresent(alExportarInforme)));
        MenuContextual.instalar(tablaAlgoritmos);
        MenuContextual.instalar(tablaEjercicios);
    }

    private java.util.Optional<Perfil> perfilDelRanking() {
        ResumenAdministracionService.PuestoRanking puesto = tablaRanking.getSelectionModel().getSelectedItem();
        if (puesto == null) {
            return java.util.Optional.empty();
        }
        return perfilesCargados.stream().filter(p -> p.getId().equals(puesto.perfilId())).findFirst();
    }

    @FXML
    private void verProgresoPerfil() {
        Perfil elegido = tablaPerfiles.getSelectionModel().getSelectedItem();
        if (elegido != null) {
            alVerProgreso.accept(elegido);
        }
    }

    @FXML
    private void exportarInformePerfil() {
        Perfil elegido = tablaPerfiles.getSelectionModel().getSelectedItem();
        if (elegido != null) {
            alExportarInforme.accept(elegido);
        }
    }

    @FXML
    private void initialize() {
        btnVerProgresoPerfil.disableProperty().bind(
                tablaPerfiles.getSelectionModel().selectedItemProperty().isNull());
        btnExportarPerfil.disableProperty().bind(
                tablaPerfiles.getSelectionModel().selectedItemProperty().isNull());
        configurarMenusContextuales();
        colRankPuesto.setCellValueFactory(c -> texto(c.getValue().puesto()));
        colRankPerfil.setCellValueFactory(c -> texto(c.getValue().nombre()));
        colRankIntentos.setCellValueFactory(c -> texto(c.getValue().intentos()));
        colRankCorrectos.setCellValueFactory(c -> texto(c.getValue().correctos()));
        colRankIncorrectos.setCellValueFactory(c -> texto(c.getValue().incorrectos()));
        colRankPorcentaje.setCellValueFactory(c -> texto(
                String.format(java.util.Locale.ROOT, "%.1f%%", c.getValue().porcentaje())));
        tablaRanking.setPlaceholder(new Label("Todavía no hay perfiles."));

        colAlgoritmoId.setCellValueFactory(c -> texto(c.getValue().getId()));
        colAlgoritmoNombre.setCellValueFactory(c -> texto(c.getValue().getNombre()));
        colAlgoritmoCategoria.setCellValueFactory(c -> texto(c.getValue().getCategoria()));
        colAlgoritmoComplejidad.setCellValueFactory(c -> texto(c.getValue().getComplejidad()));

        colEjercicioId.setCellValueFactory(c -> texto(c.getValue().getId()));
        colEjercicioAlgoritmo.setCellValueFactory(c ->
                texto(c.getValue().getAlgoritmo().getNombre()));
        colEjercicioDificultad.setCellValueFactory(c ->
                texto(c.getValue().getDificultad()));
        colEjercicioOrigen.setCellValueFactory(c -> texto(c.getValue().getOrigen()));
        colEjercicioDatos.setCellValueFactory(c ->
                texto(resumirDatos(c.getValue().getDatos())));

        colPerfilId.setCellValueFactory(c -> texto(c.getValue().getId()));
        colPerfilNombre.setCellValueFactory(c -> texto(c.getValue().getNombre()));
        colPerfilCreacion.setCellValueFactory(c -> texto(
                c.getValue().getFechaCreacion() == null
                        ? ""
                        : c.getValue().getFechaCreacion().format(FORMATO_FECHA)
        ));
        cmbGrafico.getItems().setAll(NOMBRES_GRAFICOS);
        cmbGrafico.getSelectionModel().selectedIndexProperty().addListener(
                (obs, antes, ahora) -> mostrarGrafico(ahora.intValue()));
        cmbGrafico.getSelectionModel().selectFirst();
        mostrarGrafico(0);
        cargarResumen();
    }

    private void mostrarGrafico(int indice) {
        List<Node> graficos = List.of(graficoEjerciciosAlgoritmo, graficoEjerciciosDificultad,
                graficoEjerciciosOrigen);
        if (indice < 0 || indice >= graficos.size()) {
            return;
        }
        for (int i = 0; i < graficos.size(); i++) {
            graficos.get(i).setVisible(i == indice);
            graficos.get(i).setManaged(i == indice);
        }
    }

    @FXML
    private void cargarResumen() {
        cargar(false);
    }

    @Override
    public void actualizarAutomaticamente() {
        cargar(true);
    }

    private void cargar(boolean automatico) {
        if (cargando) {
            return;
        }
        cargando = true;
        if (!automatico) {
            btnActualizar.setDisable(true);
            lblEstado.setText("Cargando datos generales…");
        }
        Thread hilo = new Thread(() -> {
            try {
                // El servicio vuelve a comprobar en la base de datos que el perfil sea administrador.
                ResumenAdministracionService.Datos datos = new ResumenAdministracionService()
                        .cargar(SesionUsuario.obtenerPerfil().getId());
                List<Algoritmo> algoritmos = datos.algoritmos();
                List<Ejercicio> ejercicios = datos.ejercicios();
                List<Perfil> perfiles = datos.perfiles();
                int totalIntentos = datos.totalIntentos();
                List<ResumenAdministracionService.PuestoRanking> ranking = datos.ranking();
                int huella = Objects.hash(
                        algoritmos.stream().map(a -> Objects.hash(
                                a.getId(), a.getNombre(), a.getCategoria(), a.getComplejidad())).toList(),
                        ejercicios.stream().map(e -> Objects.hash(
                                e.getId(), e.getAlgoritmo().getNombre(), e.getDificultad(),
                                e.getOrigen(), e.getDatos())).toList(),
                        perfiles.stream().map(p -> Objects.hash(p.getId(), p.getNombre())).toList(),
                        totalIntentos,
                        ranking);
                Platform.runLater(() -> {
                    cargando = false;
                    if (automatico && hayDatosMostrados && huella == huellaMostrada) {
                        return;
                    }
                    huellaMostrada = huella;
                    hayDatosMostrados = true;
                    tablaAlgoritmos.setItems(FXCollections.observableArrayList(algoritmos));
                    tablaEjercicios.setItems(FXCollections.observableArrayList(ejercicios));
                    Perfil seleccionado = tablaPerfiles.getSelectionModel().getSelectedItem();
                    tablaPerfiles.setItems(FXCollections.observableArrayList(perfiles));
                    if (seleccionado != null) {
                        perfiles.stream().filter(p -> p.getId().equals(seleccionado.getId())).findFirst()
                                .ifPresent(p -> tablaPerfiles.getSelectionModel().select(p));
                    }
                    perfilesCargados = perfiles;
                    tablaRanking.setItems(FXCollections.observableArrayList(ranking));
                    lblCantidadAlgoritmos.setText(Integer.toString(algoritmos.size()));
                    lblCantidadEjercicios.setText(Integer.toString(ejercicios.size()));
                    lblCantidadPerfiles.setText(Integer.toString(perfiles.size()));
                    lblCantidadIntentos.setText(Integer.toString(totalIntentos));
                    actualizarGraficos(ejercicios);
                    lblEstado.setText("Resumen actualizado.");
                    btnActualizar.setDisable(false);
                });
            } catch (SecurityException e) {
                Platform.runLater(() -> {
                    cargando = false;
                    lblEstado.setText("Acceso restringido: el resumen general es solo para administradores.");
                    btnActualizar.setDisable(true);
                });
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "No se pudo cargar el resumen general", e);
                Platform.runLater(() -> {
                    cargando = false;
                    lblEstado.setText("No se pudo cargar el resumen. Revisa PostgreSQL y el esquema.");
                    btnActualizar.setDisable(false);
                });
            }
        }, "algomentor-resumen");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void actualizarGraficos(List<Ejercicio> ejercicios) {
        Map<String, Long> porAlgoritmo = ejercicios.stream().collect(Collectors.groupingBy(
                e -> TextoInterfaz.algoritmo(e.getAlgoritmo().getNombre()),
                TreeMap::new, Collectors.counting()));
        List<PieChart.Data> rebanadas = porAlgoritmo.entrySet().stream()
                .map(entrada -> new PieChart.Data(entrada.getKey() + " (" + entrada.getValue() + ")",
                        entrada.getValue()))
                .toList();
        graficoEjerciciosAlgoritmo.setData(FXCollections.observableArrayList(rebanadas));

        Map<Integer, Long> porDificultad = ejercicios.stream().collect(Collectors.groupingBy(
                e -> e.getDificultad() == null ? 0 : e.getDificultad(),
                TreeMap::new, Collectors.counting()));
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Ejercicios");
        porDificultad.forEach((dificultad, cantidad) ->
                serie.getData().add(new XYChart.Data<>("Nivel " + dificultad, cantidad)));
        graficoEjerciciosDificultad.setData(FXCollections.observableArrayList(serie));

        Map<String, Long> porOrigen = ejercicios.stream().collect(Collectors.groupingBy(
                e -> e.getOrigen() == null || e.getOrigen().isBlank() ? "Sin origen" : e.getOrigen(),
                TreeMap::new, Collectors.counting()));
        graficoEjerciciosOrigen.setData(FXCollections.observableArrayList(
                porOrigen.entrySet().stream()
                        .map(entrada -> new PieChart.Data(entrada.getKey() + " (" + entrada.getValue() + ")",
                                entrada.getValue()))
                        .toList()));
    }

    private ReadOnlyStringWrapper texto(Object valor) {
        return new ReadOnlyStringWrapper(valor == null ? "" : valor.toString());
    }

    private String resumirDatos(String datos) {
        if (datos == null || datos.length() <= 72) {
            return datos == null ? "" : datos;
        }
        return datos.substring(0, 69) + "...";
    }
}
