package ni.edu.uam.algomentor.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import ni.edu.uam.algomentor.api.MathJsClient;
import ni.edu.uam.algomentor.engine.ClasificacionSistema;
import ni.edu.uam.algomentor.engine.Fraccion;
import ni.edu.uam.algomentor.engine.Gauss;
import ni.edu.uam.algomentor.engine.ResultadoMatriz;
import ni.edu.uam.algomentor.util.EntradaAprendizaje;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VerificarSistemaApiController {

    private static final Logger LOG = Logger.getLogger(VerificarSistemaApiController.class.getName());

    @FXML private TextArea txtMatriz;
    @FXML private Button btnConsultar;
    @FXML private Label lblLocal;
    @FXML private Label lblExterno;

    private final MathJsClient cliente = new MathJsClient();

    @FXML
    private void consultar() {
        Fraccion[][] matriz;
        ResultadoMatriz resultado;
        try {
            matriz = EntradaAprendizaje.leerMatriz(txtMatriz.getText());
            if (matriz[0].length != matriz.length + 1) {
                throw new IllegalArgumentException("Escribe una ecuación por incógnita en la matriz ampliada.");
            }
            resultado = new Gauss().ejecutar(matriz);
        } catch (IllegalArgumentException e) {
            lblLocal.setText(e.getMessage());
            lblExterno.setText("");
            return;
        }
        lblLocal.setText("AlgoMentor: " + resultado.clasificacion()
                + (resultado.solucion().isEmpty() ? "" : " · " + resultado.solucion()));
        if (resultado.clasificacion() != ClasificacionSistema.UNICA) {
            lblExterno.setText("math.js verifica sistemas con solución única.");
            return;
        }

        btnConsultar.setDisable(true);
        txtMatriz.setDisable(true);
        lblExterno.setText("Consultando math.js…");
        Task<String> tarea = new Task<>() {
            @Override
            protected String call() throws Exception {
                return cliente.resolver(matriz);
            }
        };
        tarea.setOnSucceeded(event -> {
            lblExterno.setText("math.js: " + tarea.getValue());
            btnConsultar.setDisable(false);
            txtMatriz.setDisable(false);
        });
        tarea.setOnFailed(event -> {
            LOG.log(Level.WARNING, "No se pudo consultar math.js", tarea.getException());
            lblExterno.setText("No se pudo consultar math.js. Revisa Internet e inténtalo de nuevo.");
            btnConsultar.setDisable(false);
            txtMatriz.setDisable(false);
        });
        Thread hilo = new Thread(tarea, "algomentor-mathjs");
        hilo.setDaemon(true);
        hilo.start();
    }
}
