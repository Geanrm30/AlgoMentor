package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.service.ReporteProgresoService;
import ni.edu.uam.algomentor.util.Actualizable;
import ni.edu.uam.algomentor.util.Animaciones;
import ni.edu.uam.algomentor.util.SesionUsuario;
import ni.edu.uam.algomentor.util.Tema;
import ni.edu.uam.algomentor.util.TextoInterfaz;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProgresoController implements Actualizable {

    private static final Logger LOG = Logger.getLogger(ProgresoController.class.getName());
    private static final double UMBRAL_ALTO = 70;
    private static final double UMBRAL_MEDIO = 40;
    private static final int INTENTOS_CONSULTADOS = 100;
    private static final int INTENTOS_EN_HISTORIAL = 20;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Huella de los últimos datos mostrados; permite saltar la actualización automática si nada cambió. */
    private int huellaMostrada;
    private boolean hayDatosMostrados;

    @FXML private Label lblResumen;
    @FXML private Label lblPerfilActual;
    @FXML private Button btnVistaPrevia;
    @FXML private Label lblIntentos;
    @FXML private Label lblCorrectos;
    @FXML private Label lblIncorrectos;
    @FXML private Label lblPorcentajeGeneral;
    @FXML private Label lblRacha;
    @FXML private Label lblTiempoPromedio;
    @FXML private TableView<EstadisticasDAO.RendimientoAlgoritmo> tablaRendimiento;
    @FXML private TableColumn<EstadisticasDAO.RendimientoAlgoritmo, String> colRendAlgoritmo;
    @FXML private TableColumn<EstadisticasDAO.RendimientoAlgoritmo, String> colRendIntentos;
    @FXML private TableColumn<EstadisticasDAO.RendimientoAlgoritmo, String> colRendCorrectos;
    @FXML private TableColumn<EstadisticasDAO.RendimientoAlgoritmo, String> colRendIncorrectos;
    @FXML private TableColumn<EstadisticasDAO.RendimientoAlgoritmo, Double> colRendPorcentaje;
    @FXML private StackedBarChart<String, Number> graficoAlgoritmos;
    @FXML private CategoryAxis axisAlgoritmo;
    @FXML private NumberAxis axisIntentosAlgoritmo;
    @FXML private LineChart<Number, Number> graficoEvolucion;
    @FXML private NumberAxis axisIntento;
    @FXML private NumberAxis axisAcierto;
    @FXML private PieChart graficoResultados;
    @FXML private BarChart<String, Number> graficoErrores;
    @FXML private CategoryAxis axisTipoError;
    @FXML private NumberAxis axisCantidadErrores;
    @FXML private TableView<EstadisticasDAO.IntentoReciente> tablaHistorial;
    @FXML private TableColumn<EstadisticasDAO.IntentoReciente, String> colHistFecha;
    @FXML private TableColumn<EstadisticasDAO.IntentoReciente, String> colHistAlgoritmo;
    @FXML private TableColumn<EstadisticasDAO.IntentoReciente, String> colHistModo;
    @FXML private TableColumn<EstadisticasDAO.IntentoReciente, String> colHistResultado;
    @FXML private TableColumn<EstadisticasDAO.IntentoReciente, String> colHistDuracion;
    @FXML private BarChart<String, Number> graficoHabilidades;
    @FXML private CategoryAxis axisHabilidad;
    @FXML private NumberAxis axisCantidad;
    @FXML private TableView<EstadisticasDAO.EstadisticaHabilidad> tabla;
    @FXML private TableColumn<EstadisticasDAO.EstadisticaHabilidad, String> colHabilidad;
    @FXML private TableColumn<EstadisticasDAO.EstadisticaHabilidad, String> colAciertos;
    @FXML private TableColumn<EstadisticasDAO.EstadisticaHabilidad, String> colErrores;
    @FXML private TableColumn<EstadisticasDAO.EstadisticaHabilidad, Double> colPorcentaje;

    @FXML
    private void initialize() {
        lblPerfilActual.setText("Perfil: " + SesionUsuario.obtenerPerfil().getNombre());
        colHabilidad.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().nombre()));
        colAciertos.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Integer.toString(c.getValue().aciertos())));
        colErrores.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Integer.toString(c.getValue().errores())));
        colPorcentaje.setCellValueFactory(c ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(
                        c.getValue().porcentajeAciertos()));
        colPorcentaje.setCellFactory(columna -> new CeldaPorcentaje<>());
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        axisCantidad.setForceZeroInRange(true);
        graficoHabilidades.setCategoryGap(24);
        graficoHabilidades.setBarGap(6);
        configurarTablaRendimiento();
        configurarTablaHistorial();
        axisCantidadErrores.setForceZeroInRange(true);
        axisIntentosAlgoritmo.setForceZeroInRange(true);
        graficoErrores.setCategoryGap(32);
        verProgreso();
    }

    private void configurarTablaRendimiento() {
        colRendAlgoritmo.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(TextoInterfaz.algoritmo(c.getValue().algoritmo())));
        colRendIntentos.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Long.toString(c.getValue().intentos())));
        colRendCorrectos.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Long.toString(c.getValue().correctos())));
        colRendIncorrectos.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Long.toString(c.getValue().incorrectos())));
        colRendPorcentaje.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().porcentajeAciertos()));
        colRendPorcentaje.setCellFactory(columna -> new CeldaPorcentaje<>());
        tablaRendimiento.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaRendimiento.setPlaceholder(new Label("Todavía no hay intentos con algoritmos."));
    }

    private void configurarTablaHistorial() {
        colHistFecha.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().fecha().format(FORMATO_FECHA)));
        colHistAlgoritmo.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(TextoInterfaz.algoritmo(c.getValue().algoritmo())));
        colHistModo.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(TextoInterfaz.modo(c.getValue().modo())));
        colHistResultado.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(textoResultado(c.getValue().correcto())));
        colHistDuracion.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().duracionSegundos() == null ? "—" : c.getValue().duracionSegundos() + " s"));
        tablaHistorial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaHistorial.setPlaceholder(new Label("Todavía no hay intentos registrados."));
    }

    private static String textoResultado(Boolean correcto) {
        if (correcto == null) {
            return "Sin calificar";
        }
        return correcto ? "Correcto" : "Incorrecto";
    }

    private static String etiquetaTipoError(String tipo) {
        return switch (tipo) {
            case "SIGNO" -> "Signo";
            case "FRACCION" -> "Fracción";
            case "ARITMETICO" -> "Aritmético";
            case "OPERACION_INVALIDA" -> "Operación inválida";
            default -> tipo;
        };
    }

    @FXML
    private void verProgreso() {
        cargarProgreso(false);
    }

    @Override
    public void actualizarAutomaticamente() {
        cargarProgreso(true);
    }

    private void cargarProgreso(boolean automatico) {
        Perfil perfil = SesionUsuario.obtenerPerfil();
        if (!automatico) {
            lblResumen.setText("Cargando progreso…");
        }
        ejecutarEnSegundoPlano(() -> {
            EstadisticasDAO dao = new EstadisticasDAO();
            Long perfilId = perfil.getId();
            var resumen = dao.obtenerResumenIntentos(perfilId);
            List<EstadisticasDAO.EstadisticaHabilidad> habilidades =
                    dao.listarProgresoPorHabilidad(perfilId);
            List<EstadisticasDAO.RendimientoAlgoritmo> rendimiento =
                    dao.listarRendimientoPorAlgoritmo(perfilId);
            List<EstadisticasDAO.ErrorPorTipo> errores = dao.listarErroresPorTipo(perfilId);
            List<EstadisticasDAO.IntentoReciente> recientes =
                    dao.listarIntentosRecientes(perfilId, INTENTOS_CONSULTADOS);
            OptionalDouble duracionPromedio = dao.obtenerDuracionPromedioSegundos(perfilId);
            int huella = Objects.hash(resumen, habilidades, rendimiento, errores, recientes,
                    duracionPromedio);
            Platform.runLater(() -> {
                if (automatico && hayDatosMostrados && huella == huellaMostrada) {
                    return;
                }
                huellaMostrada = huella;
                hayDatosMostrados = true;
                tabla.setItems(FXCollections.observableArrayList(habilidades));
                actualizarGrafico(habilidades);
                tablaRendimiento.setItems(FXCollections.observableArrayList(rendimiento));
                actualizarGraficoAlgoritmos(rendimiento);
                actualizarGraficoErrores(errores);
                actualizarGraficoEvolucion(recientes);
                actualizarGraficoResultados(resumen);
                tablaHistorial.setItems(FXCollections.observableArrayList(
                        recientes.subList(0, Math.min(INTENTOS_EN_HISTORIAL, recientes.size()))));
                actualizarTarjetas(resumen, recientes, duracionPromedio);
                lblResumen.setText("Última actualización: " + LocalTime.now().format(FORMATO_HORA)
                        + (resumen.sinCalificar() > 0
                        ? " · Sin calificar: " + resumen.sinCalificar() + " (simulaciones)" : "")
                        + (habilidades.isEmpty()
                        ? "\nSin progreso por habilidad registrado." : ""));
            });
        });
    }

    private void actualizarGraficoAlgoritmos(List<EstadisticasDAO.RendimientoAlgoritmo> rendimiento) {
        XYChart.Series<String, Number> correctos = new XYChart.Series<>();
        correctos.setName("Correctos");
        XYChart.Series<String, Number> incorrectos = new XYChart.Series<>();
        incorrectos.setName("Incorrectos");
        for (EstadisticasDAO.RendimientoAlgoritmo r : rendimiento) {
            String nombre = TextoInterfaz.algoritmo(r.algoritmo());
            correctos.getData().add(new XYChart.Data<>(nombre, r.correctos()));
            incorrectos.getData().add(new XYChart.Data<>(nombre, r.incorrectos()));
        }
        graficoAlgoritmos.setTitle(rendimiento.isEmpty()
                ? "Sin intentos por algoritmo" : "Resultados por algoritmo");
        graficoAlgoritmos.setData(FXCollections.observableArrayList(correctos, incorrectos));
    }

    private void actualizarGraficoEvolucion(List<EstadisticasDAO.IntentoReciente> recientes) {
        List<Double> puntos = EstadisticasDAO.precisionAcumulada(recientes);
        XYChart.Series<Number, Number> serie = new XYChart.Series<>();
        serie.setName("Acierto acumulado");
        for (int i = 0; i < puntos.size(); i++) {
            serie.getData().add(new XYChart.Data<>(i + 1, puntos.get(i)));
        }
        graficoEvolucion.setTitle(puntos.isEmpty()
                ? "Sin intentos calificados"
                : "Acierto acumulado en los últimos " + puntos.size() + " intentos calificados");
        graficoEvolucion.setData(FXCollections.observableArrayList(serie));
    }

    private void actualizarGraficoResultados(EstadisticasDAO.ResumenIntentos resumen) {
        List<PieChart.Data> datos = new ArrayList<>();
        datos.add(new PieChart.Data("Correctos (" + resumen.correctos() + ")", resumen.correctos()));
        datos.add(new PieChart.Data("Incorrectos (" + resumen.incorrectos() + ")", resumen.incorrectos()));
        datos.add(new PieChart.Data("Sin calificar (" + resumen.sinCalificar() + ")", resumen.sinCalificar()));
        graficoResultados.setTitle(resumen.total() == 0 ? "Sin intentos" : "Resultados de los intentos");
        graficoResultados.setData(FXCollections.observableArrayList(datos));
    }

    private void actualizarTarjetas(EstadisticasDAO.ResumenIntentos resumen,
                                    List<EstadisticasDAO.IntentoReciente> recientes,
                                    OptionalDouble duracionPromedio) {
        long calificados = resumen.correctos() + resumen.incorrectos();
        lblIntentos.setText(Long.toString(resumen.total()));
        lblCorrectos.setText(Long.toString(resumen.correctos()));
        lblIncorrectos.setText(Long.toString(resumen.incorrectos()));
        lblPorcentajeGeneral.setText(calificados == 0 ? "—"
                : String.format("%.1f%%", (resumen.correctos() * 100.0) / calificados));
        lblRacha.setText(Integer.toString(EstadisticasDAO.rachaActual(recientes)));
        lblTiempoPromedio.setText(duracionPromedio.isPresent()
                ? String.format("%.0f s", duracionPromedio.getAsDouble()) : "—");
    }

    private void actualizarGraficoErrores(List<EstadisticasDAO.ErrorPorTipo> errores) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Errores detectados");
        for (EstadisticasDAO.ErrorPorTipo error : errores) {
            serie.getData().add(new XYChart.Data<>(etiquetaTipoError(error.tipo()), error.cantidad()));
        }
        graficoErrores.setTitle(errores.isEmpty() ? "Sin errores registrados" : "Errores por tipo");
        graficoErrores.setData(FXCollections.observableArrayList(serie));
    }

    private void actualizarGrafico(List<EstadisticasDAO.EstadisticaHabilidad> habilidades) {
        XYChart.Series<String, Number> aciertos = new XYChart.Series<>();
        aciertos.setName("Aciertos");
        XYChart.Series<String, Number> errores = new XYChart.Series<>();
        errores.setName("Errores");
        for (EstadisticasDAO.EstadisticaHabilidad habilidad : habilidades) {
            aciertos.getData().add(new XYChart.Data<>(habilidad.nombre(), habilidad.aciertos()));
            errores.getData().add(new XYChart.Data<>(habilidad.nombre(), habilidad.errores()));
        }
        graficoHabilidades.setTitle(habilidades.isEmpty()
                ? "Sin resultados por habilidad"
                : "Aciertos y errores por habilidad");
        graficoHabilidades.setData(FXCollections.observableArrayList(aciertos, errores));
    }

    /** Permite exportar el informe PDF desde el menú Archivo. */
    public void exportarInforme() {
        generarReporte();
    }

    @FXML
    private void generarReporte() {
        Perfil perfil = SesionUsuario.obtenerPerfil();
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar informe de progreso");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documento PDF", "*.pdf"));
        String nombrePerfilArchivo = perfil.getNombre()
                .replaceAll("[\\\\/:*?\"<>|]", "_");
        selector.setInitialFileName("progreso-" + nombrePerfilArchivo + ".pdf");
        Window ventana = tabla.getScene() == null ? null : tabla.getScene().getWindow();
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
                Path generado = new ReporteProgresoService().generarReporte(perfilId, destino);
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

    @FXML
    private void previsualizarReporte() {
        Perfil perfil = SesionUsuario.obtenerPerfil();
        Long perfilId = perfil.getId();
        String nombrePerfil = perfil.getNombre();
        btnVistaPrevia.setDisable(true);
        lblResumen.setText("Preparando vista previa del reporte…");
        Thread hilo = new Thread(() -> {
            try {
                JasperPrint reporte = new ReporteProgresoService().prepararReporte(perfilId);
                SwingUtilities.invokeLater(() -> {
                    JasperViewer visor = new JasperViewer(reporte, false);
                    visor.setTitle("Vista previa del reporte - " + nombrePerfil);
                    visor.setVisible(true);
                });
                Platform.runLater(() ->
                        lblResumen.setText("Vista previa abierta para " + nombrePerfil + "."));
            } catch (JRException | IOException | RuntimeException e) {
                LOG.log(Level.SEVERE, "No se pudo preparar la vista previa del informe", e);
                Platform.runLater(() -> {
                    lblResumen.setText("No se pudo preparar la vista previa.");
                    mostrarAlerta(Alert.AlertType.ERROR, "Error al previsualizar informe",
                            "No se pudo preparar el reporte. Revisa la conexión con PostgreSQL.");
                });
            } finally {
                Platform.runLater(() -> btnVistaPrevia.setDisable(false));
            }
        }, "algomentor-vista-previa-reporte");
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

    private static final class CeldaPorcentaje<T> extends TableCell<T, Double> {

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
                Platform.runLater(() -> lblResumen.setText(
                        "No se pudo consultar el progreso. Revisa PostgreSQL y el esquema."));
            }
        }, "algomentor-progreso");
        hilo.setDaemon(true);
        hilo.start();
    }
}
