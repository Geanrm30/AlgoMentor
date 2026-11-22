package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResumenController {

    private static final Logger LOG = Logger.getLogger(ResumenController.class.getName());
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label lblCantidadAlgoritmos;
    @FXML private Label lblCantidadEjercicios;
    @FXML private Label lblCantidadPerfiles;
    @FXML private Label lblCantidadIntentos;
    @FXML private Label lblEstado;
    @FXML private Button btnActualizar;
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
        btnActualizar.setDisable(true);
        lblEstado.setText("Cargando datos generales…");
        Thread hilo = new Thread(() -> {
            try {
                List<Algoritmo> algoritmos = new AlgoritmoDAO().listar();
                List<Ejercicio> ejercicios = new EjercicioDAO().listarConAlgoritmo();
                List<Perfil> perfiles = new BaseDAO<>(Perfil.class).listar();
                List<Intento> intentos = new BaseDAO<>(Intento.class).listar();
                Platform.runLater(() -> {
                    tablaAlgoritmos.setItems(FXCollections.observableArrayList(algoritmos));
                    tablaEjercicios.setItems(FXCollections.observableArrayList(ejercicios));
                    tablaPerfiles.setItems(FXCollections.observableArrayList(perfiles));
                    lblCantidadAlgoritmos.setText(Integer.toString(algoritmos.size()));
                    lblCantidadEjercicios.setText(Integer.toString(ejercicios.size()));
                    lblCantidadPerfiles.setText(Integer.toString(perfiles.size()));
                    lblCantidadIntentos.setText(Integer.toString(intentos.size()));
                    lblEstado.setText("Resumen actualizado.");
                    btnActualizar.setDisable(false);
                });
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "No se pudo cargar el resumen general", e);
                Platform.runLater(() -> {
                    lblEstado.setText("No se pudo cargar el resumen. Revisa PostgreSQL y el esquema.");
                    btnActualizar.setDisable(false);
                });
            }
        }, "algomentor-resumen");
        hilo.setDaemon(true);
        hilo.start();
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
