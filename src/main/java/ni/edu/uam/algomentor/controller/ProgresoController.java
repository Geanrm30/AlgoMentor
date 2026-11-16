package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;
import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.service.PerfilService;
import ni.edu.uam.algomentor.service.ReporteProgresoService;
import ni.edu.uam.algomentor.util.Animaciones;
import ni.edu.uam.algomentor.util.Tema;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.JRException;

public class ProgresoController {

    private static final Logger LOG = Logger.getLogger(ProgresoController.class.getName());
    private static final double UMBRAL_ALTO = 70;
    private static final double UMBRAL_MEDIO = 40;

    @FXML private ComboBox<Perfil> cmbPerfil;
    @FXML private Label lblResumen;
    @FXML private TableView<EstadisticasDAO.EstadisticaHabilidad> tabla;
    @FXML private TableColumn<EstadisticasDAO.EstadisticaHabilidad, String> colHabilidad;
    @FXML private TableColumn<EstadisticasDAO.EstadisticaHabilidad, String> colAciertos;
    @FXML private TableColumn<EstadisticasDAO.EstadisticaHabilidad, String> colErrores;
    @FXML private TableColumn<EstadisticasDAO.EstadisticaHabilidad, Double> colPorcentaje;

    @FXML
    private void initialize() {
        cmbPerfil.setConverter(new StringConverter<>() {
            @Override public String toString(Perfil valor) { return valor == null ? "" : valor.getNombre(); }
            @Override public Perfil fromString(String texto) { return null; }
        });
        colHabilidad.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().nombre()));
        colAciertos.setCellValueFactory(c -> new ReadOnlyStringWrapper(Integer.toString(c.getValue().aciertos())));
        colErrores.setCellValueFactory(c -> new ReadOnlyStringWrapper(Integer.toString(c.getValue().errores())));
        colPorcentaje.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().porcentajeAciertos()));
        colPorcentaje.setCellFactory(columna -> new CeldaPorcentaje());
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

    @FXML
    private void generarReporte() {
        Perfil perfil = cmbPerfil.getValue();
        if (perfil == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Perfil requerido",
                    "Selecciona un perfil antes de generar el reporte.");
            return;
        }

        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar informe de progreso");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documento PDF", "*.pdf")
        );
        String nombrePerfilArchivo = perfil.getNombre()
                .replaceAll("[\\\\/:*?\"<>|]", "_");
        selector.setInitialFileName("progreso-" + nombrePerfilArchivo + ".pdf");

        Window ventana = cmbPerfil.getScene() == null
                ? null
                : cmbPerfil.getScene().getWindow();
        java.io.File archivo = selector.showSaveDialog(ventana);
        if (archivo == null) {
            return;
        }

        lblResumen.setText("Generando informe PDF…");
        Long perfilId = perfil.getId();
        Path destinoElegido = archivo.toPath();
        Path destino = destinoElegido.getFileName().toString()
                .toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")
                ? destinoElegido
                : destinoElegido.resolveSibling(destinoElegido.getFileName() + ".pdf");
        Thread hilo = new Thread(() -> {
            try {
                Path generado = new ReporteProgresoService()
                        .generarReporte(perfilId, destino);
                Platform.runLater(() -> {
                    lblResumen.setText("Informe guardado: " + generado.getFileName());
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Informe generado",
                            "El informe de progreso se guardó en:\n" + generado);
                });
            } catch (JRException | IOException | RuntimeException e) {
                LOG.log(Level.SEVERE, "No se pudo generar el informe de progreso", e);
                Platform.runLater(() -> {
                    lblResumen.setText("No se pudo generar el informe.");
                    mostrarAlerta(Alert.AlertType.ERROR, "Error al generar informe",
                            "No se pudo generar el PDF. Revisa la conexión con PostgreSQL y el destino seleccionado.");
                });
            }
        }, "algomentor-reporte-progreso");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        Tema.estilizar(alerta);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private static final class CeldaPorcentaje extends TableCell<EstadisticasDAO.EstadisticaHabilidad, Double> {

        private final ProgressBar barra = new ProgressBar(0);
        private final Label etiqueta = new Label();
        private final HBox contenido = new HBox(10, barra, etiqueta);
        private Double animado;

        private CeldaPorcentaje() {
            barra.setPrefWidth(100);
            etiqueta.getStyleClass().add("porcentaje-habilidad");
            contenido.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(Double porcentaje, boolean vacia) {
            super.updateItem(porcentaje, vacia);
            if (vacia || porcentaje == null) {
                animado = null;
                setGraphic(null);
                return;
            }
            barra.getStyleClass().removeAll("barra-ok", "barra-media", "barra-baja");
            barra.getStyleClass().add(porcentaje >= UMBRAL_ALTO ? "barra-ok"
                    : porcentaje >= UMBRAL_MEDIO ? "barra-media" : "barra-baja");
            etiqueta.setText(String.format("%.1f%%", porcentaje));
            setGraphic(contenido);
            if (!porcentaje.equals(animado)) {
                animado = porcentaje;
                Animaciones.animarProgreso(barra, porcentaje / 100);
            }
        }
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
