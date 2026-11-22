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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ni.edu.uam.algomentor.dao.AlgoritmoDAO;
import ni.edu.uam.algomentor.dao.BaseDAO;
import ni.edu.uam.algomentor.dao.EjercicioDAO;
import ni.edu.uam.algomentor.model.Algoritmo;
import ni.edu.uam.algomentor.model.Ejercicio;
import ni.edu.uam.algomentor.model.Intento;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.util.Actualizable;
import ni.edu.uam.algomentor.util.TextoInterfaz;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResumenController implements Actualizable {

    private static final Logger LOG = Logger.getLogger(ResumenController.class.getName());
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Huella de los últimos datos mostrados; evita redibujar (y perder la selección) si nada cambió. */
    private int huellaMostrada;
    private boolean hayDatosMostrados;
    private boolean cargando;

    @FXML private Label lblCantidadAlgoritmos;
    @FXML private Label lblCantidadEjercicios;
    @FXML private Label lblCantidadPerfiles;
    @FXML private Label lblCantidadIntentos;
    @FXML private Label lblEstado;
    @FXML private Button btnActualizar;
    @FXML private PieChart graficoEjerciciosAlgoritmo;
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

    @FXML
    private void initialize() {
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
        cargarResumen();
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
                List<Algoritmo> algoritmos = new AlgoritmoDAO().listar();
                List<Ejercicio> ejercicios = new EjercicioDAO().listarConAlgoritmo();
                List<Perfil> perfiles = new BaseDAO<>(Perfil.class).listar();
                List<Intento> intentos = new BaseDAO<>(Intento.class).listar();
                int huella = Objects.hash(
                        algoritmos.stream().map(a -> Objects.hash(
                                a.getId(), a.getNombre(), a.getCategoria(), a.getComplejidad())).toList(),
                        ejercicios.stream().map(e -> Objects.hash(
                                e.getId(), e.getAlgoritmo().getNombre(), e.getDificultad(),
                                e.getOrigen(), e.getDatos())).toList(),
                        perfiles.stream().map(p -> Objects.hash(p.getId(), p.getNombre())).toList(),
                        intentos.size());
                Platform.runLater(() -> {
                    cargando = false;
                    if (automatico && hayDatosMostrados && huella == huellaMostrada) {
                        return;
                    }
                    huellaMostrada = huella;
                    hayDatosMostrados = true;
                    tablaAlgoritmos.setItems(FXCollections.observableArrayList(algoritmos));
                    tablaEjercicios.setItems(FXCollections.observableArrayList(ejercicios));
                    tablaPerfiles.setItems(FXCollections.observableArrayList(perfiles));
                    lblCantidadAlgoritmos.setText(Integer.toString(algoritmos.size()));
                    lblCantidadEjercicios.setText(Integer.toString(ejercicios.size()));
                    lblCantidadPerfiles.setText(Integer.toString(perfiles.size()));
                    lblCantidadIntentos.setText(Integer.toString(intentos.size()));
                    actualizarGraficos(ejercicios);
                    lblEstado.setText("Resumen actualizado.");
                    btnActualizar.setDisable(false);
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
