package ni.edu.uam.algomentor.controller;

import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import ni.edu.uam.algomentor.api.MathJsClient;
import ni.edu.uam.algomentor.engine.ClasificacionSistema;
import ni.edu.uam.algomentor.engine.Fraccion;
import ni.edu.uam.algomentor.engine.Gauss;
import ni.edu.uam.algomentor.engine.ResultadoMatriz;
import ni.edu.uam.algomentor.util.EntradaMatrizVisual;
import ni.edu.uam.algomentor.util.TextoInterfaz;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VerificarSistemaApiController {

    private static final Logger LOG = Logger.getLogger(VerificarSistemaApiController.class.getName());

    @FXML private VBox entradaMatriz;
    @FXML private Button btnConsultar;
    @FXML private ScrollPane scrollVista;
    @FXML private Label lblEstadoConsulta;
    @FXML private Label lblLocal;
    @FXML private Label lblExterno;

    private final MathJsClient cliente = new MathJsClient();
    private EntradaMatrizVisual matrizVisual;

    @FXML
    private void initialize() {
        matrizVisual = new EntradaMatrizVisual();
        entradaMatriz.getChildren().add(matrizVisual);
    }

    @FXML
    private void consultar() {
        lblEstadoConsulta.setText("");
        lblLocal.setText("");
        lblExterno.setText("");
        Fraccion[][] matriz;
        ResultadoMatriz resultado;
        try {
            matriz = matrizVisual.leerMatriz();
            if (matriz[0].length != matriz.length + 1) {
                throw new IllegalArgumentException("Tienes " + matriz.length + " ecuaciones y "
                        + (matriz[0].length - 1) + " incógnitas. Para comparar las soluciones, ambos números deben ser iguales.");
            }
            resultado = new Gauss().ejecutar(matriz);
        } catch (IllegalArgumentException e) {
            lblEstadoConsulta.setText(e.getMessage());
            return;
        }
        lblLocal.setText("Solución de AlgoMentor: " + TextoInterfaz.clasificacion(resultado.clasificacion())
                + (resultado.solucion().isEmpty() ? "" : " · " + TextoInterfaz.solucion(resultado.solucion())));
        if (resultado.clasificacion() != ClasificacionSistema.UNICA) {
            lblExterno.setText("La comparación externa solo está disponible para sistemas con una solución.");
            lblEstadoConsulta.setText("Resultado listo. Revísalo abajo.");
            mostrarResultados();
            return;
        }

        btnConsultar.setDisable(true);
        matrizVisual.setDisable(true);
        lblEstadoConsulta.setText("Solución local lista. Consultando la segunda solución…");
        lblExterno.setText("Calculando segunda solución…");
        mostrarResultados();
        Task<String> tarea = new Task<>() {
            @Override
            protected String call() throws Exception {
                return cliente.resolver(matriz);
            }
        };
        tarea.setOnSucceeded(event -> {
            lblExterno.setText("Solución externa: " + TextoInterfaz.solucionExterna(tarea.getValue()));
            lblEstadoConsulta.setText("Comparación lista. Revisa ambas soluciones abajo.");
            btnConsultar.setDisable(false);
            matrizVisual.setDisable(false);
        });
        tarea.setOnFailed(event -> {
            LOG.log(Level.WARNING, "No se pudo consultar math.js", tarea.getException());
            lblExterno.setText("No se pudo obtener la segunda solución. Revisa tu conexión a Internet e inténtalo de nuevo.");
            lblEstadoConsulta.setText("La solución local está lista; no se pudo completar la comparación.");
            btnConsultar.setDisable(false);
            matrizVisual.setDisable(false);
        });
        Thread hilo = new Thread(tarea, "algomentor-mathjs");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarResultados() {
        Platform.runLater(() -> scrollVista.setVvalue(scrollVista.getVmax()));
    }
}
