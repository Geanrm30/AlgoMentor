package ni.edu.uam.algomentor.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;
import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.engine.FichaAlgoritmo;
import ni.edu.uam.algomentor.engine.FormatoDatos;
import ni.edu.uam.algomentor.engine.Fraccion;
import ni.edu.uam.algomentor.engine.TipoOperacion;
import ni.edu.uam.algomentor.engine.GeneradorEjercicios;
import ni.edu.uam.algomentor.engine.Paso;
import ni.edu.uam.algomentor.engine.Pseudocodigo;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimuladorController {

    private static final Logger LOG = Logger.getLogger(SimuladorController.class.getName());

    @FXML private ComboBox<String> cmbAlgoritmo;
    @FXML private TextField txtArreglo;
    @FXML private VBox entradaMatriz;
    @FXML private Label lblFormato;
    @FXML private Label lblMecanica;
    @FXML private HBox contenedorArreglo;
    @FXML private ScrollPane scrollArreglo;
    @FXML private VBox contenedorMatriz;
    @FXML private Label lblPaso;
    @FXML private Label lblExplicacion;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Button btnReproducir;
    @FXML private ComboBox<String> cmbVelocidad;
    @FXML private CheckBox chkBarras;
    @FXML private HBox contenedorBarras;
    @FXML private ScrollPane scrollBarras;
    @FXML private VBox panelPseudocodigo;
    @FXML private VBox listaPseudocodigo;

    private ResultadoEjecucion resultado;
    private int indice = -1;
    private int indiceMostrado = -1;
    private boolean matricial;
    private String conclusion = "";
    private String[] celdasPrevias;
    private EntradaMatrizVisual matrizVisual;
    private Timeline reproduccion;
    private Pseudocodigo pseudocodigo;

    private static final List<String> VELOCIDADES = List.of(
            "Lenta (1,6 s por paso)", "Normal (0,9 s por paso)", "Rápida (0,4 s por paso)", "Muy rápida (0,15 s)");
    private static final int[] MILISEGUNDOS = {1600, 900, 400, 150};

    @FXML
    private void initialize() {
        matrizVisual = new EntradaMatrizVisual();
        entradaMatriz.getChildren().add(matrizVisual);
        cmbVelocidad.setItems(FXCollections.observableArrayList(VELOCIDADES));
        cmbVelocidad.getSelectionModel().select(1);
        cmbVelocidad.valueProperty().addListener((obs, antes, ahora) -> {
            if (reproduccion != null) {
                detenerReproduccion();
                alternarReproduccion();
            }
        });
        chkBarras.selectedProperty().addListener((obs, antes, ahora) -> {
            scrollArreglo.setVisible(!ahora);
            scrollArreglo.setManaged(!ahora);
            scrollBarras.setVisible(ahora);
            scrollBarras.setManaged(ahora);
            if (resultado != null && !matricial) {
                celdasPrevias = null;
                actualizar();
            }
        });
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
            CatalogoAlgoritmos elegido = CatalogoAlgoritmos.porNombre(actual);
            matricial = !elegido.esOrdenamiento();
            cargarPseudocodigo(elegido);
            chkBarras.setVisible(!matricial);
            chkBarras.setManaged(!matricial);
            String mecanica = FichaAlgoritmo.de(elegido).map(FichaAlgoritmo::mecanica).orElse("");
            lblMecanica.setText(mecanica);
            lblMecanica.setVisible(!mecanica.isEmpty());
            lblMecanica.setManaged(!mecanica.isEmpty());
            txtArreglo.setVisible(!matricial);
            txtArreglo.setManaged(!matricial);
            entradaMatriz.setVisible(matricial);
            entradaMatriz.setManaged(matricial);
            lblFormato.setText(matricial
                    ? "Completa los coeficientes y el término independiente b. Admite fracciones como 1/2."
                    : "Escribe enteros separados por coma. Desplaza la fila para ver arreglos grandes.");
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
        detenerReproduccion();
        try {
            CatalogoAlgoritmos algoritmo = CatalogoAlgoritmos.porNombre(cmbAlgoritmo.getValue());
            if (matricial) {
                Fraccion[][] entrada = matrizVisual.leerMatriz();
                ResultadoMatriz matriz = algoritmo.crearMatricial().ejecutar(entrada);
                resultado = matriz.ejecucion();
                conclusion = "Sistema: " + TextoInterfaz.clasificacion(matriz.clasificacion())
                        + (matriz.solucion().isEmpty() ? "" : " · " + TextoInterfaz.solucion(matriz.solucion()));
            } else {
                int[] entrada = EntradaAprendizaje.leerArregloSinLimite(txtArreglo.getText());
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
        detenerReproduccion();
        if (resultado != null && indice >= 0) {
            indice--;
            actualizar();
        }
    }

    @FXML
    private void siguiente() {
        detenerReproduccion();
        avanzar();
    }

    private void avanzar() {
        if (resultado != null && indice + 1 < resultado.pasos().size()) {
            indice++;
            actualizar();
        }
    }

    @FXML
    private void alternarReproduccion() {
        if (resultado == null) {
            return;
        }
        if (reproduccion != null) {
            detenerReproduccion();
            return;
        }
        if (indice + 1 >= resultado.pasos().size()) {
            indice = -1;
            actualizar();
        }
        int posicion = Math.max(0, cmbVelocidad.getSelectionModel().getSelectedIndex());
        reproduccion = new Timeline(new KeyFrame(Duration.millis(MILISEGUNDOS[posicion]), evento -> {
            avanzar();
            if (resultado == null || indice + 1 >= resultado.pasos().size()) {
                detenerReproduccion();
            }
        }));
        reproduccion.setCycleCount(Animation.INDEFINITE);
        reproduccion.play();
        btnReproducir.setText("Pausar");
    }

    private void detenerReproduccion() {
        if (reproduccion != null) {
            reproduccion.stop();
            reproduccion = null;
        }
        btnReproducir.setText("Reproducir");
    }

    private void cargarPseudocodigo(CatalogoAlgoritmos algoritmo) {
        pseudocodigo = Pseudocodigo.de(algoritmo).orElse(null);
        listaPseudocodigo.getChildren().clear();
        panelPseudocodigo.setVisible(pseudocodigo != null);
        panelPseudocodigo.setManaged(pseudocodigo != null);
        if (pseudocodigo == null) {
            return;
        }
        for (String linea : pseudocodigo.lineas()) {
            Label etiqueta = new Label(linea.isEmpty() ? " " : linea);
            etiqueta.getStyleClass().add("linea-pseudocodigo");
            etiqueta.setMaxWidth(Double.MAX_VALUE);
            listaPseudocodigo.getChildren().add(etiqueta);
        }
    }

    private void resaltarLinea(int linea) {
        for (int i = 0; i < listaPseudocodigo.getChildren().size(); i++) {
            Node etiqueta = listaPseudocodigo.getChildren().get(i);
            etiqueta.getStyleClass().remove("linea-activa");
            if (i == linea) {
                etiqueta.getStyleClass().add("linea-activa");
            }
        }
    }

    private void actualizar() {
        Paso paso = indice < 0 ? null : resultado.pasos().get(indice);
        TipoOperacion movimiento = paso == null ? null : paso.tipo();
        if (indiceMostrado >= 0 && indice == indiceMostrado - 1) {
            movimiento = resultado.pasos().get(indiceMostrado).tipo();
        }
        String estado = paso == null ? resultado.estadoInicial() : paso.estadoPosterior();
        if (matricial) {
            mostrarMatriz(FormatoDatos.leerEstadoMatriz(estado), paso == null ? List.of() : paso.indices());
        } else {
            mostrarArreglo(EntradaAprendizaje.leerArregloSinLimite(estado.substring(1, estado.length() - 1)),
                    paso == null ? List.of() : paso.indices(),
                    paso == null ? -1 : paso.indicePivote(), movimiento);
        }
        indiceMostrado = indice;
        lblPaso.setText(paso == null ? "Estado inicial" : "Paso " + paso.numero() + " de " + resultado.pasos().size() + " · " + TextoInterfaz.operacion(paso.tipo()));
        lblExplicacion.setText(paso == null ? "Pulsa Siguiente para recorrer los pasos." : paso.explicacion()
                + (indice == resultado.pasos().size() - 1 ? "\n" + conclusion : ""));
        Animaciones.entrar(lblExplicacion, 0, 6);
        resaltarLinea(pseudocodigo == null || paso == null ? -1 : pseudocodigo.lineaPara(paso.tipo()));
        btnAnterior.setDisable(indice < 0);
        btnSiguiente.setDisable(indice + 1 >= resultado.pasos().size());
        btnReproducir.setDisable(false);
    }

    private HBox contenedorActivo() {
        return chkBarras.isSelected() ? contenedorBarras : contenedorArreglo;
    }

    private ScrollPane scrollActivo() {
        return chkBarras.isSelected() ? scrollBarras : scrollArreglo;
    }

    private void mostrarArreglo(int[] valores, List<Integer> destacados, int pivote, TipoOperacion movimiento) {
        contenedorMatriz.getChildren().clear();
        HBox contenedor = contenedorActivo();
        (contenedor == contenedorBarras ? contenedorArreglo : contenedorBarras).getChildren().clear();
        double[] xPrevias = posicionesX(contenedor);
        contenedor.getChildren().clear();
        String[] textos = new String[valores.length];
        int minimo = java.util.Arrays.stream(valores).min().orElse(0);
        int maximo = java.util.Arrays.stream(valores).max().orElse(0);
        for (int i = 0; i < valores.length; i++) {
            textos[i] = Integer.toString(valores[i]);
            boolean activo = destacados.contains(i);
            Node elemento;
            if (contenedor == contenedorBarras) {
                elemento = barra(valores[i], minimo, maximo, valores.length, activo, i == pivote);
            } else {
                Label valor = celda(textos[i], activo);
                if (i == pivote) {
                    valor.getStyleClass().add("valor-pivote");
                }
                elemento = valor;
            }
            if (i == pivote) {
                elemento.setAccessibleText("Pivote: " + textos[i]);
            }
            contenedor.getChildren().add(elemento);
        }
        animarArreglo(contenedor, textos, destacados, xPrevias, movimiento);
        seguirElemento(destacados, pivote);
        celdasPrevias = textos;
    }

    /** Barra con altura proporcional al valor, entre 24 y 150 píxeles. */
    private Node barra(int valor, int minimo, int maximo, int cantidad, boolean activa, boolean pivote) {
        double altura = maximo == minimo ? 90 : 24 + (valor - (double) minimo) / ((double) maximo - minimo) * 126;
        Region cuerpo = new Region();
        cuerpo.setMinHeight(altura);
        cuerpo.setPrefHeight(altura);
        cuerpo.setMaxHeight(altura);
        double ancho = cantidad > 24 ? 16 : 38;
        cuerpo.setMinWidth(ancho);
        cuerpo.setPrefWidth(ancho);
        cuerpo.getStyleClass().add(pivote ? "barra-pivote" : activa ? "barra-activa" : "barra");
        VBox columna = new VBox(4);
        columna.setAlignment(Pos.BOTTOM_CENTER);
        if (cantidad <= 24) {
            Label etiqueta = new Label(Integer.toString(valor));
            etiqueta.getStyleClass().add("barra-valor");
            columna.getChildren().add(etiqueta);
        }
        columna.getChildren().add(cuerpo);
        return columna;
    }

    private void seguirElemento(List<Integer> destacados, int pivote) {
        int objetivo = destacados.isEmpty() ? pivote : destacados.get(destacados.size() - 1);
        int pasoActual = indice;
        HBox contenedor = contenedorActivo();
        ScrollPane desplazamiento = scrollActivo();
        Platform.runLater(() -> {
            if (indice != pasoActual) return;
            desplazamiento.applyCss();
            desplazamiento.layout();
            double ancho = contenedor.getWidth();
            double visible = desplazamiento.getViewportBounds().getWidth();
            if (objetivo < 0 || objetivo >= contenedor.getChildren().size() || ancho <= visible) {
                desplazamiento.setHvalue(0);
                return;
            }
            Node celda = contenedor.getChildren().get(objetivo);
            double centro = celda.getBoundsInParent().getMinX() + celda.getBoundsInParent().getWidth() / 2;
            double corrimiento = Math.max(0, Math.min(ancho - visible, centro - visible / 2));
            desplazamiento.setHvalue(corrimiento / (ancho - visible));
        });
    }

    private void animarArreglo(HBox contenedor, String[] textos, List<Integer> destacados, double[] xPrevias,
                               TipoOperacion movimiento) {
        List<Node> celdas = contenedor.getChildren();
        if (celdasPrevias == null || celdasPrevias.length != textos.length) {
            aparecerEscalonado(celdas);
            return;
        }
        contenedor.applyCss();
        contenedor.layout();
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
        detenerReproduccion();
        resaltarLinea(-1);
        btnReproducir.setDisable(true);
        contenedorBarras.getChildren().clear();
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
