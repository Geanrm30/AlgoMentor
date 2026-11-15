package ni.edu.uam.algomentor.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ni.edu.uam.algomentor.engine.BubbleSort;
import ni.edu.uam.algomentor.engine.FormatoDatos;
import ni.edu.uam.algomentor.engine.Fraccion;
import ni.edu.uam.algomentor.engine.Gauss;
import ni.edu.uam.algomentor.engine.GaussJordan;
import ni.edu.uam.algomentor.engine.InsertionSort;
import ni.edu.uam.algomentor.engine.Paso;
import ni.edu.uam.algomentor.engine.ResultadoEjecucion;
import ni.edu.uam.algomentor.engine.ResultadoMatriz;
import ni.edu.uam.algomentor.util.EntradaAprendizaje;

import java.util.List;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimuladorController {

    private static final Logger LOG = Logger.getLogger(SimuladorController.class.getName());

    @FXML private ComboBox<String> cmbAlgoritmo;
    @FXML private TextField txtArreglo;
    @FXML private TextArea txtMatriz;
    @FXML private Label lblFormato;
    @FXML private HBox contenedorArreglo;
    @FXML private VBox contenedorMatriz;
    @FXML private Label lblPaso;
    @FXML private Label lblExplicacion;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Button btnCargarAudio;
    @FXML private Button btnAudio;
    @FXML private Label lblAudio;

    private ResultadoEjecucion resultado;
    private int indice = -1;
    private boolean matricial;
    private String conclusion = "";
    private MediaPlayer reproductor;
    private int pasoConAudio = -1;

    @FXML
    private void initialize() {
        cmbAlgoritmo.setItems(FXCollections.observableArrayList("Bubble Sort", "Insertion Sort", "Gauss", "Gauss-Jordan"));
        cmbAlgoritmo.valueProperty().addListener((obs, anterior, actual) -> {
            limpiarResultado();
            matricial = actual.startsWith("Gauss");
            txtArreglo.setVisible(!matricial);
            txtArreglo.setManaged(!matricial);
            txtMatriz.setVisible(matricial);
            txtMatriz.setManaged(matricial);
            lblFormato.setText(matricial
                    ? "Matriz ampliada: una ecuación por línea; coeficientes y resultado separados con comas. Admite fracciones como 1/2."
                    : "Hasta 12 enteros separados por coma.");
        });
        cmbAlgoritmo.getSelectionModel().selectFirst();
    }

    @FXML
    private void simular() {
        try {
            String algoritmo = cmbAlgoritmo.getValue();
            if (matricial) {
                Fraccion[][] entrada = EntradaAprendizaje.leerMatriz(txtMatriz.getText());
                ResultadoMatriz matriz = "Gauss".equals(algoritmo)
                        ? new Gauss().ejecutar(entrada) : new GaussJordan().ejecutar(entrada);
                resultado = matriz.ejecucion();
                conclusion = "Sistema: " + matriz.clasificacion()
                        + (matriz.solucion().isEmpty() ? "" : " · solución: " + matriz.solucion());
            } else {
                int[] entrada = EntradaAprendizaje.leerArreglo(txtArreglo.getText());
                resultado = "Bubble Sort".equals(algoritmo)
                        ? new BubbleSort().ejecutar(entrada) : new InsertionSort().ejecutar(entrada);
                conclusion = "Resultado: " + resultado.estadoFinal();
            }
            indice = -1;
            actualizar();
        } catch (IllegalArgumentException e) {
            limpiarResultado();
            mostrarAlerta(Alert.AlertType.WARNING, "Entrada inválida", e.getMessage());
        } catch (RuntimeException e) {
            limpiarResultado();
            LOG.log(Level.SEVERE, "Falló la simulación", e);
            mostrarAlerta(Alert.AlertType.ERROR, "Error de simulación", "No se pudo completar la simulación.");
        }
    }

    @FXML
    private void anterior() {
        if (resultado != null && indice >= 0) {
            indice--;
            actualizar();
        }
    }

    @FXML
    private void siguiente() {
        if (resultado != null && indice + 1 < resultado.pasos().size()) {
            indice++;
            actualizar();
        }
    }

    private void actualizar() {
        if (pasoConAudio != indice) {
            liberarAudio();
        }
        Paso paso = indice < 0 ? null : resultado.pasos().get(indice);
        String estado = paso == null ? resultado.estadoInicial() : paso.estadoPosterior();
        if (matricial) {
            mostrarMatriz(FormatoDatos.leerEstadoMatriz(estado), paso == null ? List.of() : paso.indices());
        } else {
            mostrarArreglo(EntradaAprendizaje.leerArreglo(estado.substring(1, estado.length() - 1)),
                    paso == null ? List.of() : paso.indices());
        }
        lblPaso.setText(paso == null ? "Estado inicial" : "Paso " + paso.numero() + " de " + resultado.pasos().size() + " · " + paso.tipo());
        lblExplicacion.setText(paso == null ? "Pulsa Siguiente para recorrer los pasos." : paso.explicacion()
                + (indice == resultado.pasos().size() - 1 ? "\n" + conclusion : ""));
        btnAnterior.setDisable(indice < 0);
        btnSiguiente.setDisable(indice + 1 >= resultado.pasos().size());
        btnCargarAudio.setDisable(indice < 0);
    }

    @FXML
    private void cargarAudio() {
        if (resultado == null || indice < 0) {
            return;
        }
        FileChooser selector = new FileChooser();
        selector.setTitle("Audio del paso " + (indice + 1));
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio MP3 o WAV", "*.mp3", "*.wav"));
        File archivo = selector.showOpenDialog(btnCargarAudio.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        try {
            liberarAudio();
            reproductor = new MediaPlayer(new Media(archivo.toURI().toString()));
            MediaPlayer actual = reproductor;
            pasoConAudio = indice;
            reproductor.setOnError(() -> {
                LOG.log(Level.WARNING, "No se pudo reproducir el audio", actual.getError());
                lblAudio.setText("El audio no se pudo reproducir. La explicación escrita sigue disponible.");
                btnAudio.setDisable(true);
            });
            reproductor.setOnEndOfMedia(() -> {
                actual.stop();
                btnAudio.setText("Reproducir");
            });
            lblAudio.setText("Audio del paso " + (indice + 1) + ": " + archivo.getName());
            btnAudio.setDisable(false);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Archivo de audio no válido", e);
            liberarAudio();
            mostrarAlerta(Alert.AlertType.WARNING, "Audio no disponible", "No se pudo abrir el archivo seleccionado.");
        }
    }

    @FXML
    private void reproducirAudio() {
        if (reproductor == null) {
            return;
        }
        if (reproductor.getStatus() == MediaPlayer.Status.PLAYING) {
            reproductor.pause();
            btnAudio.setText("Reproducir");
        } else {
            if (reproductor.getStatus() == MediaPlayer.Status.STOPPED) {
                reproductor.seek(Duration.ZERO);
            }
            reproductor.play();
            btnAudio.setText("Pausar");
        }
    }

    private void liberarAudio() {
        if (reproductor != null) {
            reproductor.stop();
            reproductor.dispose();
            reproductor = null;
        }
        pasoConAudio = -1;
        btnAudio.setDisable(true);
        btnAudio.setText("Reproducir");
        lblAudio.setText("La explicación escrita está disponible para cada paso. Puedes adjuntar un audio MP3 o WAV.");
    }

    private void mostrarArreglo(int[] valores, List<Integer> destacados) {
        contenedorMatriz.getChildren().clear();
        contenedorArreglo.getChildren().clear();
        for (int i = 0; i < valores.length; i++) {
            contenedorArreglo.getChildren().add(celda(Integer.toString(valores[i]), destacados.contains(i)));
        }
    }

    private void mostrarMatriz(Fraccion[][] matriz, List<Integer> destacados) {
        contenedorArreglo.getChildren().clear();
        contenedorMatriz.getChildren().clear();
        for (int i = 0; i < matriz.length; i++) {
            HBox fila = new HBox(8);
            fila.setAlignment(Pos.CENTER);
            for (Fraccion valor : matriz[i]) {
                fila.getChildren().add(celda(valor.toString(), destacados.contains(i)));
            }
            contenedorMatriz.getChildren().add(fila);
        }
    }

    private Label celda(String valor, boolean activa) {
        Label celda = new Label(valor);
        celda.getStyleClass().add(activa ? "valor-activo" : "valor-arreglo");
        celda.setMinWidth(52);
        celda.setMinHeight(48);
        celda.setAlignment(Pos.CENTER);
        return celda;
    }

    private void limpiarResultado() {
        liberarAudio();
        resultado = null;
        indice = -1;
        contenedorArreglo.getChildren().clear();
        contenedorMatriz.getChildren().clear();
        lblPaso.setText("Prepara una entrada para empezar.");
        lblExplicacion.setText("");
        btnAnterior.setDisable(true);
        btnSiguiente.setDisable(true);
        btnCargarAudio.setDisable(true);
    }

    public void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
