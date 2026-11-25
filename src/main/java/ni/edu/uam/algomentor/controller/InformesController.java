package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import ni.edu.uam.algomentor.dao.AlgoritmoDAO;
import ni.edu.uam.algomentor.model.Algoritmo;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.service.InformeAdministrativo;
import ni.edu.uam.algomentor.service.InformeDominioAdministrativo;
import ni.edu.uam.algomentor.service.PerfilService;
import ni.edu.uam.algomentor.service.RegistroInformes;
import ni.edu.uam.algomentor.util.SesionUsuario;
import ni.edu.uam.algomentor.util.Tema;
import ni.edu.uam.algomentor.util.TextoInterfaz;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel de informes de Administración: lista los informes registrados en {@link RegistroInformes} y deja ver
 * su vista previa o guardarlos en PDF. Cada informe vuelve a comprobar el rol en el servicio.
 */
public class InformesController {

    private static final Logger LOG = Logger.getLogger(InformesController.class.getName());

    @FXML private ListView<InformeAdministrativo> listaInformes;
    @FXML private Label lblTitulo;
    @FXML private Label lblDescripcion;
    @FXML private Label lblIncluye;
    @FXML private Label lblEstado;
    @FXML private Button btnVistaPrevia;
    @FXML private Button btnExportar;

    private static final double CELDA = 52;

    private boolean ocupado;

    @FXML
    private void initialize() {
        listaInformes.setItems(FXCollections.observableArrayList(RegistroInformes.todos()));
        listaInformes.setFixedCellSize(CELDA);
        listaInformes.prefHeightProperty().bind(Bindings.size(listaInformes.getItems()).multiply(CELDA).add(12));
        listaInformes.setCellFactory(lista -> new ListCell<>() {
            @Override
            protected void updateItem(InformeAdministrativo informe, boolean vacio) {
                super.updateItem(informe, vacio);
                setText(vacio || informe == null ? null : informe.titulo());
            }
        });
        listaInformes.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> mostrarDetalle(actual));
        listaInformes.getSelectionModel().selectFirst();
        if (listaInformes.getSelectionModel().isEmpty()) {
            mostrarDetalle(null);
        }
    }

    private void mostrarDetalle(InformeAdministrativo informe) {
        lblEstado.setText("");
        if (informe == null) {
            lblTitulo.setText("Selecciona un informe");
            lblDescripcion.setText("");
            lblIncluye.setText("");
        } else {
            lblTitulo.setText(informe.titulo());
            lblDescripcion.setText(informe.descripcion());
            lblIncluye.setText(informe.secciones().stream()
                    .map(seccion -> "•  " + seccion)
                    .collect(Collectors.joining("\n")));
        }
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean habilitado = listaInformes.getSelectionModel().getSelectedItem() != null && !ocupado;
        btnVistaPrevia.setDisable(!habilitado);
        btnExportar.setDisable(!habilitado);
    }

    private void ocupar(boolean valor) {
        ocupado = valor;
        actualizarBotones();
    }

    @FXML
    private void vistaPrevia() {
        InformeAdministrativo informe = listaInformes.getSelectionModel().getSelectedItem();
        if (informe == null || ocupado) {
            return;
        }
        Long solicitanteId = SesionUsuario.obtenerPerfil().getId();
        long[] seleccionDominio = null;
        if (informe instanceof InformeDominioAdministrativo) {
            Optional<long[]> elegidos = elegirPerfilYAlgoritmo();
            if (elegidos.isEmpty()) {
                return;
            }
            seleccionDominio = elegidos.get();
        }
        final long[] seleccion = seleccionDominio;
        ocupar(true);
        lblEstado.setText("Preparando la vista previa…");
        Thread hilo = new Thread(() -> {
            try {
                JasperPrint reporte = informe instanceof InformeDominioAdministrativo dominio && seleccion != null
                        ? dominio.prepararReporte(solicitanteId, seleccion[0], seleccion[1])
                        : informe.prepararReporte(solicitanteId);
                SwingUtilities.invokeLater(() -> {
                    JasperViewer visor = new JasperViewer(reporte, false);
                    visor.setTitle("Vista previa - " + informe.titulo());
                    visor.setVisible(true);
                });
                Platform.runLater(() -> lblEstado.setText("Vista previa abierta."));
            } catch (SecurityException e) {
                Platform.runLater(() -> avisar(Alert.AlertType.WARNING, "Sin permiso", e.getMessage()));
            } catch (JRException | IOException | RuntimeException e) {
                LOG.log(Level.SEVERE, "No se pudo preparar la vista previa de " + informe.id(), e);
                Platform.runLater(() -> {
                    lblEstado.setText("No se pudo preparar la vista previa.");
                    avisar(Alert.AlertType.ERROR, "Error en la vista previa",
                            "No se pudo preparar el informe. Revisa la conexión con PostgreSQL.");
                });
            } finally {
                Platform.runLater(() -> ocupar(false));
            }
        }, "algomentor-informe-vista-previa");
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void exportar() {
        InformeAdministrativo informe = listaInformes.getSelectionModel().getSelectedItem();
        if (informe == null || ocupado) {
            return;
        }
        Long solicitanteId = SesionUsuario.obtenerPerfil().getId();
        long[] seleccionDominio = null;
        if (informe instanceof InformeDominioAdministrativo) {
            Optional<long[]> elegidos = elegirPerfilYAlgoritmo();
            if (elegidos.isEmpty()) {
                return;
            }
            seleccionDominio = elegidos.get();
        }
        final long[] seleccion = seleccionDominio;
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar informe");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF", "*.pdf"));
        selector.setInitialFileName(informe.nombreArchivoSugerido());
        Window ventana = listaInformes.getScene() == null ? null : listaInformes.getScene().getWindow();
        java.io.File archivo = selector.showSaveDialog(ventana);
        if (archivo == null) {
            return;
        }
        Path elegido = archivo.toPath();
        Path destino = elegido.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf")
                ? elegido : elegido.resolveSibling(elegido.getFileName() + ".pdf");
        ocupar(true);
        lblEstado.setText("Generando el PDF…");
        Thread hilo = new Thread(() -> {
            try {
                Path generado = informe instanceof InformeDominioAdministrativo dominio && seleccion != null
                        ? dominio.guardarPdf(solicitanteId, seleccion[0], seleccion[1], destino)
                        : informe.guardarPdf(solicitanteId, destino);
                Platform.runLater(() -> {
                    lblEstado.setText("Informe guardado: " + generado.getFileName());
                    avisar(Alert.AlertType.INFORMATION, "Informe generado",
                            "El informe se guardó en:\n" + generado);
                });
            } catch (SecurityException e) {
                Platform.runLater(() -> avisar(Alert.AlertType.WARNING, "Sin permiso", e.getMessage()));
            } catch (JRException | IOException | RuntimeException e) {
                LOG.log(Level.SEVERE, "No se pudo generar el informe " + informe.id(), e);
                Platform.runLater(() -> {
                    lblEstado.setText("No se pudo generar el informe.");
                    avisar(Alert.AlertType.ERROR, "Error al generar el informe",
                            "No se pudo generar el PDF. Revisa PostgreSQL y el destino seleccionado.");
                });
            } finally {
                Platform.runLater(() -> ocupar(false));
            }
        }, "algomentor-informe-pdf");
        hilo.setDaemon(true);
        hilo.start();
    }

    private Optional<long[]> elegirPerfilYAlgoritmo() {
        List<Perfil> perfiles;
        List<Algoritmo> algoritmos;
        try {
            perfiles = new PerfilService().listarPerfiles().stream()
                    .sorted(Comparator.comparing(p -> p.getNombre().toLowerCase(Locale.ROOT)))
                    .toList();
            algoritmos = new AlgoritmoDAO().listar().stream()
                    .sorted(Comparator.comparing(a -> TextoInterfaz.algoritmo(a.getNombre()).toLowerCase(Locale.ROOT)))
                    .toList();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "No se pudieron cargar perfiles o algoritmos para la constancia", e);
            avisar(Alert.AlertType.ERROR, "Error de conexión",
                    "No se pudieron cargar los perfiles y algoritmos desde PostgreSQL.");
            return Optional.empty();
        }
        if (perfiles.isEmpty() || algoritmos.isEmpty()) {
            avisar(Alert.AlertType.WARNING, "Datos insuficientes",
                    "Se requiere al menos un perfil y un algoritmo registrados para generar la constancia.");
            return Optional.empty();
        }

        Dialog<long[]> dialogo = new Dialog<>();
        Tema.estilizar(dialogo);
        dialogo.setTitle("Constancia de dominio");
        dialogo.setHeaderText("Elige el perfil y el algoritmo a evaluar");
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Perfil> cmbPerfil = new ComboBox<>(FXCollections.observableArrayList(perfiles));
        cmbPerfil.setPrefWidth(260);
        cmbPerfil.setConverter(new StringConverter<>() {
            @Override
            public String toString(Perfil perfil) {
                return perfil == null ? "" : perfil.getNombre();
            }

            @Override
            public Perfil fromString(String s) {
                return null;
            }
        });
        Long actualId = SesionUsuario.obtenerPerfil().getId();
        perfiles.stream().filter(p -> p.getId().equals(actualId)).findFirst()
                .ifPresentOrElse(cmbPerfil::setValue, () -> cmbPerfil.getSelectionModel().selectFirst());

        ComboBox<Algoritmo> cmbAlgoritmo = new ComboBox<>(FXCollections.observableArrayList(algoritmos));
        cmbAlgoritmo.setPrefWidth(260);
        cmbAlgoritmo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Algoritmo algoritmo) {
                return algoritmo == null ? "" : TextoInterfaz.algoritmo(algoritmo.getNombre());
            }

            @Override
            public Algoritmo fromString(String s) {
                return null;
            }
        });
        cmbAlgoritmo.getSelectionModel().selectFirst();

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.addRow(0, new Label("Perfil:"), cmbPerfil);
        grid.addRow(1, new Label("Algoritmo:"), cmbAlgoritmo);
        dialogo.getDialogPane().setContent(grid);

        dialogo.setResultConverter(boton -> {
            if (boton == ButtonType.OK && cmbPerfil.getValue() != null && cmbAlgoritmo.getValue() != null) {
                return new long[]{cmbPerfil.getValue().getId(), cmbAlgoritmo.getValue().getId()};
            }
            return null;
        });
        return dialogo.showAndWait();
    }

    private void avisar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        Tema.estilizar(alerta);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
