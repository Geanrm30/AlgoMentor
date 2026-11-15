package ni.edu.uam.algomentor.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import ni.edu.uam.algomentor.api.OpenTriviaClient;
import ni.edu.uam.algomentor.api.OpenTriviaQuestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RepasoApiController {

    private static final Logger LOG = Logger.getLogger(RepasoApiController.class.getName());
    private static final int CANTIDAD_PREGUNTAS = 5;

    @FXML private Button btnCargar;
    @FXML private Button btnVerificar;
    @FXML private Button btnSiguiente;
    @FXML private Label lblEstado;
    @FXML private Label lblProgreso;
    @FXML private Label lblPregunta;
    @FXML private Label lblResultado;
    @FXML private VBox contenedorRespuestas;

    private final OpenTriviaClient cliente = new OpenTriviaClient();
    private List<OpenTriviaQuestion> preguntas = List.of();
    private int indicePregunta;
    private int respuestasCorrectas;
    private boolean respuestaVerificada;

    @FXML
    private void cargarPreguntas() {
        btnCargar.setDisable(true);
        btnVerificar.setDisable(true);
        btnSiguiente.setDisable(true);
        lblEstado.setText("Consultando Open Trivia DB…");
        lblProgreso.setText("Cargando preguntas…");
        lblPregunta.setText("");
        lblResultado.setText("");
        contenedorRespuestas.getChildren().clear();

        Task<List<OpenTriviaQuestion>> task = new Task<>() {
            @Override
            protected List<OpenTriviaQuestion> call() throws Exception {
                return cliente.obtenerPreguntasComputacion(CANTIDAD_PREGUNTAS).results();
            }
        };
        task.setOnSucceeded(event -> {
            preguntas = task.getValue();
            indicePregunta = 0;
            respuestasCorrectas = 0;
            btnCargar.setDisable(false);
            if (preguntas.isEmpty()) {
                lblEstado.setText("La API no devolvió preguntas. Intenta de nuevo más tarde.");
                lblProgreso.setText("Sin preguntas disponibles.");
                return;
            }
            lblEstado.setText("Preguntas cargadas desde Open Trivia DB.");
            mostrarPreguntaActual();
        });
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            LOG.log(Level.WARNING, "No se pudieron cargar preguntas de Open Trivia DB", error);
            btnCargar.setDisable(false);
            lblEstado.setText("No se pudieron cargar preguntas: "
                    + (error == null ? "error desconocido" : error.getMessage()));
            lblProgreso.setText("Sin preguntas disponibles.");
        });
        Thread hilo = new Thread(task, "algomentor-open-trivia");
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void verificarRespuesta() {
        RadioButton seleccion = (RadioButton) contenedorRespuestas.getChildren()
                .stream()
                .filter(node -> node instanceof RadioButton radio && radio.isSelected())
                .findFirst()
                .orElse(null);
        if (seleccion == null) {
            lblResultado.setText("Selecciona una respuesta antes de verificar.");
            return;
        }

        OpenTriviaQuestion pregunta = preguntas.get(indicePregunta);
        respuestaVerificada = true;
        btnVerificar.setDisable(true);
        contenedorRespuestas.setDisable(true);
        if (seleccion.getText().equals(pregunta.correctAnswer())) {
            respuestasCorrectas++;
            lblResultado.setText("¡Correcto!");
        } else {
            lblResultado.setText("Incorrecto. La respuesta correcta es: "
                    + pregunta.correctAnswer());
        }
        btnSiguiente.setDisable(false);
    }

    @FXML
    private void siguientePregunta() {
        if (!respuestaVerificada) {
            return;
        }
        indicePregunta++;
        if (indicePregunta >= preguntas.size()) {
            lblProgreso.setText("Repaso terminado: " + respuestasCorrectas + " de "
                    + preguntas.size() + " respuestas correctas.");
            lblPregunta.setText("¡Terminaste el repaso!");
            lblResultado.setText("");
            contenedorRespuestas.getChildren().clear();
            btnSiguiente.setDisable(true);
            return;
        }
        mostrarPreguntaActual();
    }

    private void mostrarPreguntaActual() {
        OpenTriviaQuestion pregunta = preguntas.get(indicePregunta);
        List<String> opciones = new ArrayList<>(pregunta.incorrectAnswers());
        opciones.add(pregunta.correctAnswer());
        Collections.shuffle(opciones);

        ToggleGroup grupo = new ToggleGroup();
        contenedorRespuestas.getChildren().clear();
        for (String opcion : opciones) {
            RadioButton radio = new RadioButton(opcion);
            radio.setToggleGroup(grupo);
            radio.setWrapText(true);
            contenedorRespuestas.getChildren().add(radio);
        }
        respuestaVerificada = false;
        lblProgreso.setText("Pregunta " + (indicePregunta + 1) + " de " + preguntas.size()
                + " · Correctas: " + respuestasCorrectas);
        lblPregunta.setText(pregunta.question());
        lblResultado.setText("");
        contenedorRespuestas.setDisable(false);
        btnVerificar.setDisable(false);
        btnSiguiente.setDisable(true);
    }
}
