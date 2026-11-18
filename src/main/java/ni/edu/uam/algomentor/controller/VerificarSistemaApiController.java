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
import java.util.Locale;

public class VerificarSistemaApiController {

    private static final Logger LOG = Logger.getLogger(VerificarSistemaApiController.class.getName());

    @FXML private VBox entradaMatriz;
    @FXML private Button btnConsultar;
    @FXML private ScrollPane scrollVista;
    @FXML private Label lblEstadoConsulta;
    @FXML private Label lblLocal;
    @FXML private Label lblTiempoLocal;
    @FXML private Label lblExterno;
    @FXML private Label lblTiempoExterno;

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
        lblTiempoLocal.setText("");
        lblExterno.setText("");
        lblTiempoExterno.setText("");
        Fraccion[][] matriz;
        ResultadoMatriz resultado;
        try {
            matriz = matrizVisual.leerMatriz();
            if (matriz[0].length != matriz.length + 1) {
                throw new IllegalArgumentException("Tienes " + matriz.length + " ecuaciones y "
                        + (matriz[0].length - 1) + " incógnitas. Para comparar las soluciones, ambos números deben ser iguales.");
            }
            long inicioLocal = System.nanoTime();
            resultado = new Gauss().ejecutar(matriz);
            lblTiempoLocal.setText("Tiempo de cálculo de AlgoMentor: " + formatoTiempo(System.nanoTime() - inicioLocal));
        } catch (IllegalArgumentException e) {
            lblEstadoConsulta.setText(e.getMessage());
            return;
        }
        lblLocal.setText("Solución de AlgoMentor: " + TextoInterfaz.clasificacion(resultado.clasificacion())
                + (resultado.solucion().isEmpty() ? "" : " · " + TextoInterfaz.solucionDecimal(resultado.solucion())));
        if (resultado.clasificacion() != ClasificacionSistema.UNICA) {
            lblExterno.setText("La comparación externa solo está disponible para sistemas con una solución.");
            lblTiempoExterno.setText("Tiempo externo: no aplica.");
            lblEstadoConsulta.setText("Resultado listo. Revísalo abajo.");
            mostrarResultados();
            return;
        }

        btnConsultar.setDisable(true);
        matrizVisual.setDisable(true);
        lblEstadoConsulta.setText("Solución local lista. Consultando la segunda solución…");
        lblExterno.setText("Calculando segunda solución…");
        mostrarResultados();
        Task<ResultadoExterno> tarea = new Task<>() {
            @Override
            protected ResultadoExterno call() throws Exception {
                long inicioExterno = System.nanoTime();
                String solucion = cliente.resolver(matriz);
                return new ResultadoExterno(solucion, System.nanoTime() - inicioExterno);
            }
        };
        tarea.setOnSucceeded(event -> {
            lblExterno.setText("Solución externa: " + TextoInterfaz.solucionExterna(tarea.getValue().solucion()));
            lblTiempoExterno.setText("Tiempo de consulta externa (incluye Internet): "
                    + formatoTiempo(tarea.getValue().duracionNanos()));
            lblEstadoConsulta.setText("Comparación lista. Revisa ambas soluciones abajo.");
            btnConsultar.setDisable(false);
            matrizVisual.setDisable(false);
        });
        tarea.setOnFailed(event -> {
            LOG.log(Level.WARNING, "No se pudo consultar math.js", tarea.getException());
            lblExterno.setText("No se pudo obtener la segunda solución. Revisa tu conexión a Internet e inténtalo de nuevo.");
            lblTiempoExterno.setText("Tiempo externo: no disponible porque la consulta falló.");
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

    private String formatoTiempo(long nanos) {
        return String.format(Locale.ROOT, "%.3f ms", nanos / 1_000_000.0);
    }

    private record ResultadoExterno(String solucion, long duracionNanos) { }
}
