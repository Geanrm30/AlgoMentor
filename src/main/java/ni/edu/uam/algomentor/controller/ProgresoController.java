package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;
import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.service.PerfilService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProgresoController {

    private static final Logger LOG = Logger.getLogger(ProgresoController.class.getName());

    @FXML private ComboBox<Perfil> cmbPerfil;
    @FXML private Label lblResumen;
    @FXML private TableView<EstadisticasDAO.EstadisticaHabilidad> tabla;
    @FXML private TableColumn<EstadisticasDAO.EstadisticaHabilidad, String> colHabilidad;
    @FXML private TableColumn<EstadisticasDAO.EstadisticaHabilidad, String> colAciertos;
    @FXML private TableColumn<EstadisticasDAO.EstadisticaHabilidad, String> colErrores;
    @FXML private TableColumn<EstadisticasDAO.EstadisticaHabilidad, String> colPorcentaje;

    @FXML
    private void initialize() {
        cmbPerfil.setConverter(new StringConverter<>() {
            @Override public String toString(Perfil valor) { return valor == null ? "" : valor.getNombre(); }
            @Override public Perfil fromString(String texto) { return null; }
        });
        colHabilidad.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().nombre()));
        colAciertos.setCellValueFactory(c -> new ReadOnlyStringWrapper(Integer.toString(c.getValue().aciertos())));
        colErrores.setCellValueFactory(c -> new ReadOnlyStringWrapper(Integer.toString(c.getValue().errores())));
        colPorcentaje.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.format("%.1f%%", c.getValue().porcentajeAciertos())));
        cargarPerfiles();
    }

    @FXML
    private void cargarPerfiles() {
        lblResumen.setText("Cargando perfiles…");
        ejecutarEnSegundoPlano(() -> {
            List<Perfil> perfiles = new PerfilService().listarPerfiles();
            Platform.runLater(() -> {
                cmbPerfil.setItems(FXCollections.observableArrayList(perfiles));
                tabla.getItems().clear();
                lblResumen.setText(perfiles.isEmpty() ? "Todavía no hay perfiles." : "Selecciona un perfil.");
            });
        });
    }

    @FXML
    private void verProgreso() {
        Perfil perfil = cmbPerfil.getValue();
        if (perfil == null) {
            lblResumen.setText("Selecciona un perfil.");
            return;
        }
        lblResumen.setText("Cargando progreso…");
        ejecutarEnSegundoPlano(() -> {
            EstadisticasDAO dao = new EstadisticasDAO();
            var resumen = dao.obtenerResumenIntentos(perfil.getId());
            var habilidades = dao.listarProgresoPorHabilidad(perfil.getId());
            Platform.runLater(() -> {
                tabla.setItems(FXCollections.observableArrayList(habilidades));
                lblResumen.setText("Intentos: " + resumen.total() + " · Correctos: " + resumen.correctos()
                        + " · Incorrectos: " + resumen.incorrectos() + " · Sin calificar: " + resumen.sinCalificar()
                        + (habilidades.isEmpty() ? "\nSin progreso por habilidad registrado." : ""));
            });
        });
    }

    private void ejecutarEnSegundoPlano(Runnable accion) {
        Thread hilo = new Thread(() -> {
            try {
                accion.run();
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "No se pudo consultar el progreso", e);
                Platform.runLater(() -> lblResumen.setText("No se pudo consultar el progreso. Revisa PostgreSQL y el esquema."));
            }
        }, "algomentor-progreso");
        hilo.setDaemon(true);
        hilo.start();
    }
}
