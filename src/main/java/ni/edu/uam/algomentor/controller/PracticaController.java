package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import ni.edu.uam.algomentor.dao.EjercicioDAO;
import ni.edu.uam.algomentor.model.Ejercicio;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.service.EvaluadorPractica;
import ni.edu.uam.algomentor.service.IntentoService;
import ni.edu.uam.algomentor.service.PerfilService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PracticaController {

    private static final Logger LOG = Logger.getLogger(PracticaController.class.getName());

    @FXML private ComboBox<Perfil> cmbPerfil;
    @FXML private ComboBox<Ejercicio> cmbEjercicio;
    @FXML private TextField txtNuevoPerfil;
    @FXML private TextField txtRespuesta;
    @FXML private Label lblEnunciado;
    @FXML private Label lblResultado;
    @FXML private Button btnGuardar;

    private EvaluadorPractica.Resultado evaluacion;
    private String respuestaEvaluada;
    private long inicioNanos;

    @FXML
    private void initialize() {
        cmbPerfil.setConverter(new StringConverter<>() {
            @Override public String toString(Perfil valor) { return valor == null ? "" : valor.getNombre(); }
            @Override public Perfil fromString(String texto) { return null; }
        });
        cmbEjercicio.setConverter(new StringConverter<>() {
            @Override public String toString(Ejercicio valor) {
                return valor == null ? "" : "#" + valor.getId() + " · " + valor.getAlgoritmo().getNombre();
            }
            @Override public Ejercicio fromString(String texto) { return null; }
        });
        cmbEjercicio.valueProperty().addListener((obs, anterior, actual) -> {
            invalidarEvaluacion();
            inicioNanos = System.nanoTime();
            lblEnunciado.setText(actual == null ? "Selecciona un ejercicio de la base de datos."
                    : "Algoritmo: " + actual.getAlgoritmo().getNombre() + "\nDatos: " + actual.getDatos());
        });
        txtRespuesta.textProperty().addListener((obs, anterior, actual) -> invalidarEvaluacion());
        recargar();
    }

    @FXML
    private void recargar() {
        lblResultado.setText("Cargando perfiles y ejercicios…");
        ejecutarEnSegundoPlano(() -> {
            List<Perfil> perfiles = new PerfilService().listarPerfiles();
            List<Ejercicio> ejercicios = new EjercicioDAO().listarConAlgoritmo().stream()
                    .filter(e -> List.of("Bubble Sort", "Insertion Sort", "Gauss", "Gauss-Jordan")
                            .contains(e.getAlgoritmo().getNombre()))
                    .toList();
            Platform.runLater(() -> {
                cmbPerfil.setItems(FXCollections.observableArrayList(perfiles));
                cmbEjercicio.setItems(FXCollections.observableArrayList(ejercicios));
                lblResultado.setText(ejercicios.isEmpty()
                        ? "No hay ejercicios compatibles. Crea uno en Gestión de contenido."
                        : "Selecciona un perfil y un ejercicio.");
            });
        }, "No se pudieron cargar los datos. Revisa PostgreSQL y vuelve a intentar.");
    }

    @FXML
    private void crearPerfil() {
        String nombre = txtNuevoPerfil.getText();
        try {
            PerfilService.validarNombre(nombre);
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Nombre inválido", e.getMessage());
            return;
        }
        ejecutarEnSegundoPlano(() -> {
            Perfil perfil = new PerfilService().crearPerfil(nombre);
            Platform.runLater(() -> {
                cmbPerfil.getItems().add(perfil);
                cmbPerfil.setValue(perfil);
                txtNuevoPerfil.clear();
                lblResultado.setText("Perfil creado.");
            });
        }, "No se pudo crear el perfil. Puede que ya exista o que PostgreSQL no esté disponible.");
    }

    @FXML
    private void verificar() {
        Ejercicio ejercicio = cmbEjercicio.getValue();
        if (ejercicio == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Falta ejercicio", "Selecciona un ejercicio.");
            return;
        }
        try {
            evaluacion = EvaluadorPractica.evaluar(ejercicio.getAlgoritmo().getNombre(),
                    ejercicio.getDatos(), txtRespuesta.getText());
            respuestaEvaluada = txtRespuesta.getText();
            lblResultado.setText((evaluacion.correcto() ? "Correcto." : "Incorrecto.")
                    + " Respuesta esperada: " + evaluacion.esperado());
            btnGuardar.setDisable(false);
        } catch (IllegalArgumentException e) {
            invalidarEvaluacion();
            mostrarAlerta(Alert.AlertType.WARNING, "Revisa el ejercicio", e.getMessage());
        }
    }

    @FXML
    private void guardarIntento() {
        Perfil perfil = cmbPerfil.getValue();
        Ejercicio ejercicio = cmbEjercicio.getValue();
        if (perfil == null || ejercicio == null || evaluacion == null || !txtRespuesta.getText().equals(respuestaEvaluada)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Datos incompletos", "Selecciona un perfil y verifica la respuesta.");
            return;
        }
        boolean correcto = evaluacion.correcto();
        String respuesta = respuestaEvaluada;
        int duracion = (int) Math.min(Integer.MAX_VALUE, Math.max(0, (System.nanoTime() - inicioNanos) / 1_000_000_000L));
        btnGuardar.setDisable(true);
        lblResultado.setText("Guardando intento…");
        ejecutarEnSegundoPlano(() -> {
            new IntentoService().registrarIntento(perfil.getId(), ejercicio.getId(), "PRACTICA",
                    correcto, duracion, List.of(new IntentoService.PasoRegistrado(1, respuesta, correcto)));
            Platform.runLater(() -> lblResultado.setText("Intento guardado. " + (correcto ? "Correcto." : "Incorrecto.")));
        }, "No se pudo guardar el intento. Revisa la conexión y el esquema.");
    }

    private void invalidarEvaluacion() {
        evaluacion = null;
        respuestaEvaluada = null;
        btnGuardar.setDisable(true);
    }

    private void ejecutarEnSegundoPlano(Runnable accion, String mensajeError) {
        Thread hilo = new Thread(() -> {
            try {
                accion.run();
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, mensajeError, e);
                Platform.runLater(() -> lblResultado.setText(mensajeError));
            }
        }, "algomentor-persistencia");
        hilo.setDaemon(true);
        hilo.start();
    }

    public void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
