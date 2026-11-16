package ni.edu.uam.algomentor.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import ni.edu.uam.algomentor.api.EjerciciosApiClient;
import ni.edu.uam.algomentor.api.EjerciciosApiServer;
import ni.edu.uam.algomentor.api.ResultadoImportacion;
import ni.edu.uam.algomentor.util.Animaciones;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IntercambioEjerciciosController {

    private static final Logger LOG = Logger.getLogger(IntercambioEjerciciosController.class.getName());
    private static final int MAX_BYTES = 1_048_576;

    @FXML private Button btnExportar;
    @FXML private Button btnImportar;
    @FXML private Label lblEstado;

    private final EjerciciosApiClient cliente = new EjerciciosApiClient();

    @FXML
    private void initialize() {
        try {
            EjerciciosApiServer.iniciar();
            lblEstado.setText("API local disponible en http://127.0.0.1:8765");
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo iniciar la API de ejercicios", e);
            lblEstado.setText("No se pudo iniciar la API. El puerto 8765 puede estar ocupado.");
            btnExportar.setDisable(true);
            btnImportar.setDisable(true);
        }
    }

    @FXML
    private void exportar() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar ejercicios");
        selector.setInitialFileName("ejercicios-algomentor.json");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo JSON", "*.json"));
        java.io.File archivo = selector.showSaveDialog(btnExportar.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        Path destino = archivo.toPath();
        if (!destino.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".json")) {
            destino = destino.resolveSibling(destino.getFileName() + ".json");
        }
        Path finalDestino = destino;
        ejecutar(() -> {
            byte[] contenido = cliente.exportar();
            Files.write(finalDestino, contenido);
            return "Ejercicios exportados a " + finalDestino;
        });
    }

    @FXML
    private void importar() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Importar ejercicios");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo JSON", "*.json"));
        java.io.File archivo = selector.showOpenDialog(btnImportar.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        ejecutar(() -> {
            if (Files.size(archivo.toPath()) > MAX_BYTES) {
                throw new IOException("El archivo supera 1 MB.");
            }
            ResultadoImportacion resultado = cliente.importar(Files.readAllBytes(archivo.toPath()));
            return "Importados: " + resultado.importados() + " · Ya existentes: " + resultado.existentes()
                    + ". Pulsa Actualizar en Práctica o Gestión de contenido.";
        });
    }

    private void ejecutar(Callable<String> accion) {
        btnExportar.setDisable(true);
        btnImportar.setDisable(true);
        Animaciones.mostrarResultado(lblEstado, "Procesando ejercicios…", Animaciones.Estado.NEUTRO);
        Task<String> tarea = new Task<>() {
            @Override
            protected String call() throws Exception {
                return accion.call();
            }
        };
        tarea.setOnSucceeded(event -> {
            btnExportar.setDisable(false);
            btnImportar.setDisable(false);
            Animaciones.mostrarResultado(lblEstado, tarea.getValue(), Animaciones.Estado.CORRECTO);
        });
        tarea.setOnFailed(event -> {
            btnExportar.setDisable(false);
            btnImportar.setDisable(false);
            LOG.log(Level.WARNING, "Falló el intercambio de ejercicios", tarea.getException());
            String mensaje = tarea.getException() == null ? "Error desconocido" : tarea.getException().getMessage();
            Animaciones.mostrarResultado(lblEstado, mensaje, Animaciones.Estado.ERROR);
        });
        Thread hilo = new Thread(tarea, "algomentor-intercambio-ejercicios");
        hilo.setDaemon(true);
        hilo.start();
    }
}
