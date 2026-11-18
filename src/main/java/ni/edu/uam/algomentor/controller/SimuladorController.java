package ni.edu.uam.algomentor.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.engine.FormatoDatos;
import ni.edu.uam.algomentor.engine.Fraccion;
import ni.edu.uam.algomentor.engine.TipoOperacion;
import ni.edu.uam.algomentor.engine.GeneradorEjercicios;
import ni.edu.uam.algomentor.engine.Paso;
import ni.edu.uam.algomentor.engine.ResultadoEjecucion;
import ni.edu.uam.algomentor.engine.ResultadoMatriz;
import ni.edu.uam.algomentor.util.Animaciones;
import ni.edu.uam.algomentor.util.EntradaAprendizaje;
import ni.edu.uam.algomentor.util.EntradaMatrizVisual;
import ni.edu.uam.algomentor.util.MovimientoCeldas;
import ni.edu.uam.algomentor.util.Tema;
import ni.edu.uam.algomentor.util.TextoInterfaz;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimuladorController {

    private static final Logger LOG = Logger.getLogger(SimuladorController.class.getName());

    @FXML private ComboBox<String> cmbAlgoritmo;
    @FXML private TextField txtArreglo;
    @FXML private VBox entradaMatriz;
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
    private int indiceMostrado = -1;
    private boolean matricial;
    private String conclusion = "";
    private MediaPlayer reproductor;
    private int pasoConAudio = -1;
    private String[] celdasPrevias;
    private EntradaMatrizVisual matrizVisual;

    @FXML
    private void initialize() {
        matrizVisual = new EntradaMatrizVisual();
        entradaMatriz.getChildren().add(matrizVisual);
        cmbAlgoritmo.setItems(FXCollections.observableArrayList(CatalogoAlgoritmos.nombres()));
        cmbAlgoritmo.setConverter(new StringConverter<>() {
            @Override public String toString(String valor) { return valor == null ? "" : TextoInterfaz.algoritmo(valor); }
            @Override public String fromString(String texto) { return texto; }
        });
        cmbAlgoritmo.valueProperty().addListener((obs, anterior, actual) -> {
            limpiarResultado();
            if (actual == null) {
                return;
            }
            matricial = !CatalogoAlgoritmos.porNombre(actual).esOrdenamiento();
            txtArreglo.setVisible(!matricial);
            txtArreglo.setManaged(!matricial);
            entradaMatriz.setVisible(matricial);
            entradaMatriz.setManaged(matricial);
            lblFormato.setText(matricial
                    ? "Completa los coeficientes y el término independiente b. Admite fracciones como 1/2."
                    : "Hasta 12 enteros separados por coma.");
        });
        cmbAlgoritmo.getSelectionModel().selectFirst();
    }

    @FXML
    private void generarEntrada() {
        try {
            String datos = new GeneradorEjercicios().datosPara(cmbAlgoritmo.getValue(), 2);
            limpiarResultado();
            if (matricial) matrizVisual.cargar(FormatoDatos.leerMatriz(datos));
            else txtArreglo.setText(java.util.Arrays.stream(FormatoDatos.leerArreglo(datos))
                    .mapToObj(Integer::toString).collect(java.util.stream.Collectors.joining(", ")));
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "No se pudo generar", e.getMessage());
        }
    }

    @FXML
    private void simular() {
        try {
            CatalogoAlgoritmos algoritmo = CatalogoAlgoritmos.porNombre(cmbAlgoritmo.getValue());
            if (matricial) {
                Fraccion[][] entrada = matrizVisual.leerMatriz();
                ResultadoMatriz matriz = algoritmo.crearMatricial().ejecutar(entrada);
                resultado = matriz.ejecucion();
                conclusion = "Sistema: " + TextoInterfaz.clasificacion(matriz.clasificacion())
                        + (matriz.solucion().isEmpty() ? "" : " · " + TextoInterfaz.solucion(matriz.solucion()));
            } else {
                int[] entrada = EntradaAprendizaje.leerArreglo(txtArreglo.getText());
                resultado = algoritmo.crearOrdenamiento().ejecutar(entrada);
                conclusion = "Resultado: " + resultado.estadoFinal();
            }
            indice = -1;
            celdasPrevias = null;
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
        TipoOperacion movimiento = paso == null ? null : paso.tipo();
        if (indiceMostrado >= 0 && indice == indiceMostrado - 1) {
            movimiento = resultado.pasos().get(indiceMostrado).tipo();
        }
        String estado = paso == null ? resultado.estadoInicial() : paso.estadoPosterior();
        if (matricial) {
            mostrarMatriz(FormatoDatos.leerEstadoMatriz(estado), paso == null ? List.of() : paso.indices());
        } else {
            mostrarArreglo(EntradaAprendizaje.leerArreglo(estado.substring(1, estado.length() - 1)),
                    paso == null ? List.of() : paso.indices(),
                    paso == null ? -1 : paso.indicePivote(), movimiento);
        }
        indiceMostrado = indice;
        lblPaso.setText(paso == null ? "Estado inicial" : "Paso " + paso.numero() + " de " + resultado.pasos().size() + " · " + TextoInterfaz.operacion(paso.tipo()));
        lblExplicacion.setText(paso == null ? "Pulsa Siguiente para recorrer los pasos." : paso.explicacion()
                + (indice == resultado.pasos().size() - 1 ? "\n" + conclusion : ""));
        Animaciones.entrar(lblExplicacion, 0, 6);
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

    private void mostrarArreglo(int[] valores, List<Integer> destacados, int pivote, TipoOperacion movimiento) {
        contenedorMatriz.getChildren().clear();
        double[] xPrevias = posicionesX(contenedorArreglo);
        contenedorArreglo.getChildren().clear();
        String[] textos = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            textos[i] = Integer.toString(valores[i]);
            Label valor = celda(textos[i], destacados.contains(i));
            if (i == pivote) {
                valor.getStyleClass().add("valor-pivote");
                valor.setAccessibleText("Pivote: " + textos[i]);
            }
            contenedorArreglo.getChildren().add(valor);
        }
        animarArreglo(textos, destacados, xPrevias, movimiento);
        celdasPrevias = textos;
    }

    private void animarArreglo(String[] textos, List<Integer> destacados, double[] xPrevias, TipoOperacion movimiento) {
        List<Node> celdas = contenedorArreglo.getChildren();
        if (celdasPrevias == null || celdasPrevias.length != textos.length) {
            aparecerEscalonado(celdas);
            return;
        }
        contenedorArreglo.applyCss();
        contenedorArreglo.layout();
        int[] origen = MovimientoCeldas.origenes(celdasPrevias, textos, movimiento);
        for (int i = 0; i < celdas.size(); i++) {
            Node celda = celdas.get(i);
            if (origen[i] == -1) {
                Animaciones.aparecerConRebote(celda, 0);
            } else if (origen[i] != i) {
                Animaciones.deslizarDesde(celda, xPrevias[origen[i]] - celda.getLayoutX());
            } else if (destacados.contains(i)) {
                Animaciones.pulso(celda);
            }
        }
    }

    private void mostrarMatriz(Fraccion[][] matriz, List<Integer> destacados) {
        contenedorArreglo.getChildren().clear();
        contenedorMatriz.getChildren().clear();
        List<String> textos = new ArrayList<>();
        List<Node> celdas = new ArrayList<>();
        for (int i = 0; i < matriz.length; i++) {
            HBox fila = new HBox(8);
            fila.setAlignment(Pos.CENTER);
            fila.getStyleClass().add("fila-matriz");
            for (Fraccion valor : matriz[i]) {
                Label celda = celda(valor.toString(), destacados.contains(i));
                textos.add(valor.toString());
                celdas.add(celda);
                fila.getChildren().add(celda);
            }
            contenedorMatriz.getChildren().add(fila);
        }
        String[] actuales = textos.toArray(new String[0]);
        if (celdasPrevias == null || celdasPrevias.length != actuales.length) {
            aparecerEscalonado(celdas);
        } else {
            for (int k = 0; k < actuales.length; k++) {
                if (!actuales[k].equals(celdasPrevias[k])) {
                    celdas.get(k).getStyleClass().add("valor-cambiado");
                    Animaciones.aparecerConRebote(celdas.get(k), 0);
                }
            }
        }
        celdasPrevias = actuales;
    }

    private void aparecerEscalonado(List<? extends Node> celdas) {
        for (int i = 0; i < celdas.size(); i++) {
            Animaciones.aparecerConRebote(celdas.get(i), Math.min(i * 40.0, 400));
        }
    }

    private double[] posicionesX(HBox contenedor) {
        double[] posiciones = new double[contenedor.getChildren().size()];
        for (int i = 0; i < posiciones.length; i++) {
            posiciones[i] = contenedor.getChildren().get(i).getLayoutX();
        }
        return posiciones;
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
        indiceMostrado = -1;
        celdasPrevias = null;
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
        Tema.estilizar(alerta);
        alerta.setHeaderText(null);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
