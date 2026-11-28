package ni.edu.uam.algomentor.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import javafx.util.StringConverter;
import ni.edu.uam.algomentor.engine.AlgoritmosExtra;
import ni.edu.uam.algomentor.engine.EstadoVisual;
import ni.edu.uam.algomentor.engine.GrafoEjemplo;
import ni.edu.uam.algomentor.engine.PasoExtra;
import ni.edu.uam.algomentor.engine.ResultadoExtra;
import ni.edu.uam.algomentor.util.Animaciones;
import ni.edu.uam.algomentor.util.Tema;

import java.util.List;

/** Simulador de búsquedas, recorridos de grafos y recursividad. */
public class OtrosAlgoritmosController {

    @FXML private ComboBox<AlgoritmosExtra> cmbAlgoritmo;
    @FXML private Label lblDescripcion;
    @FXML private Label lblDetalle;
    @FXML private TextField txtEntrada;
    @FXML private Label lblAyuda;
    @FXML private StackPane escenario;
    @FXML private Label lblPaso;
    @FXML private Label lblExplicacion;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Button btnReproducir;

    private ResultadoExtra resultado;
    private int indice = -1;
    private Timeline reproduccion;

    @FXML
    private void initialize() {
        cmbAlgoritmo.setItems(FXCollections.observableArrayList(AlgoritmosExtra.values()));
        cmbAlgoritmo.setConverter(new StringConverter<>() {
            @Override public String toString(AlgoritmosExtra valor) { return valor == null ? "" : valor.etiqueta(); }
            @Override public AlgoritmosExtra fromString(String texto) { return null; }
        });
        cmbAlgoritmo.valueProperty().addListener((obs, antes, ahora) -> {
            limpiar();
            if (ahora != null) {
                lblDescripcion.setText(ahora.descripcion());
                lblDetalle.setText("Complejidad: " + ahora.complejidad() + ". " + ahora.cuandoUsarlo());
                lblAyuda.setText(ahora.ayudaEntrada());
                txtEntrada.setText(ahora.entradaEjemplo());
            }
        });
        cmbAlgoritmo.getSelectionModel().selectFirst();
    }

    @FXML
    private void usarEjemplo() {
        txtEntrada.setText(cmbAlgoritmo.getValue().entradaEjemplo());
    }

    @FXML
    private void simular() {
        detenerReproduccion();
        try {
            resultado = cmbAlgoritmo.getValue().ejecutar(txtEntrada.getText());
            indice = -1;
            actualizar();
        } catch (IllegalArgumentException e) {
            limpiar();
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            Tema.estilizar(alerta);
            alerta.setHeaderText(null);
            alerta.setTitle("Entrada inválida");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
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
        reproduccion = new Timeline(new KeyFrame(Duration.millis(900), evento -> {
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

    private void limpiar() {
        detenerReproduccion();
        resultado = null;
        indice = -1;
        escenario.getChildren().clear();
        lblPaso.setText("Prepara una entrada para empezar.");
        lblExplicacion.setText("");
        btnAnterior.setDisable(true);
        btnSiguiente.setDisable(true);
        btnReproducir.setDisable(true);
    }

    private void actualizar() {
        int total = resultado.pasos().size();
        if (indice < 0) {
            escenario.getChildren().clear();
            lblPaso.setText("Estado inicial");
            lblExplicacion.setText("Pulsa Siguiente o Reproducir para recorrer los pasos.");
        } else {
            PasoExtra paso = resultado.pasos().get(indice);
            lblPaso.setText("Paso " + paso.numero() + " de " + total);
            lblExplicacion.setText(paso.explicacion() + (indice == total - 1 ? "\n" + resultado.conclusion() : ""));
            dibujar(paso.estado());
            Animaciones.entrar(lblExplicacion, 0, 6);
        }
        btnAnterior.setDisable(indice < 0);
        btnSiguiente.setDisable(indice + 1 >= total);
        btnReproducir.setDisable(false);
    }

    private void dibujar(EstadoVisual estado) {
        Node vista = switch (estado) {
            case EstadoVisual.Celdas celdas -> dibujarCeldas(celdas);
            case EstadoVisual.Grafo grafo -> dibujarGrafo(grafo);
            case EstadoVisual.Torres torres -> dibujarTorres(torres);
        };
        escenario.getChildren().setAll(vista);
    }

    private Node dibujarCeldas(EstadoVisual.Celdas estado) {
        HBox fila = new HBox(8);
        fila.setAlignment(Pos.CENTER);
        for (int i = 0; i < estado.valores().size(); i++) {
            Label celda = new Label(estado.valores().get(i));
            celda.getStyleClass().add(estado.activos().contains(i) ? "valor-activo" : "valor-arreglo");
            if (estado.encontrados().contains(i)) celda.getStyleClass().add("valor-ok");
            if (estado.descartados().contains(i)) celda.getStyleClass().add("valor-descartado");
            celda.setMinSize(52, 48);
            celda.setAlignment(Pos.CENTER);
            Label indiceCelda = new Label(Integer.toString(i + 1));
            indiceCelda.getStyleClass().add("subtitle");
            VBox columna = new VBox(4, celda, indiceCelda);
            columna.setAlignment(Pos.CENTER);
            fila.getChildren().add(columna);
            if (estado.activos().contains(i)) Animaciones.pulso(celda);
        }
        javafx.scene.control.ScrollPane desplazamiento = new javafx.scene.control.ScrollPane(fila);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setFitToHeight(true);
        desplazamiento.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.getStyleClass().add("transparente");
        return desplazamiento;
    }

    private Node dibujarGrafo(EstadoVisual.Grafo estado) {
        Pane lienzo = new Pane();
        double ancho = Math.max(escenario.getWidth(), 640);
        double alto = 300;
        double margen = 50;
        List<String> nodos = GrafoEjemplo.NODOS;
        for (GrafoEjemplo.Arista arista : GrafoEjemplo.ARISTAS) {
            double[] p = GrafoEjemplo.POSICIONES[nodos.indexOf(arista.a())];
            double[] q = GrafoEjemplo.POSICIONES[nodos.indexOf(arista.b())];
            double x1 = margen + p[0] * (ancho - 2 * margen);
            double y1 = margen + p[1] * (alto - 2 * margen);
            double x2 = margen + q[0] * (ancho - 2 * margen);
            double y2 = margen + q[1] * (alto - 2 * margen);
            Line linea = new Line(x1, y1, x2, y2);
            linea.getStyleClass().add(estado.aristasActivas().contains(arista.clave()) ? "arista-activa" : "arista");
            Label peso = new Label(Integer.toString(arista.peso()));
            peso.getStyleClass().add("arista-peso");
            peso.relocate((x1 + x2) / 2 - 8, (y1 + y2) / 2 - 11);
            lienzo.getChildren().addAll(linea, peso);
        }
        for (int i = 0; i < nodos.size(); i++) {
            String nodo = nodos.get(i);
            double x = margen + GrafoEjemplo.POSICIONES[i][0] * (ancho - 2 * margen);
            double y = margen + GrafoEjemplo.POSICIONES[i][1] * (alto - 2 * margen);
            Circle circulo = new Circle(x, y, 24);
            String situacion = estado.estados().get(nodo);
            circulo.getStyleClass().add("nodo-" + (situacion == null ? "nuevo" : situacion));
            Label nombre = new Label(nodo);
            nombre.getStyleClass().add("nodo-nombre");
            nombre.relocate(x - 6, y - 11);
            lienzo.getChildren().addAll(circulo, nombre);
            String etiqueta = estado.etiquetas().get(nodo);
            if (etiqueta != null) {
                Label dato = new Label(etiqueta);
                dato.getStyleClass().add("nodo-dato");
                dato.relocate(x - 6, y + 28);
                lienzo.getChildren().add(dato);
            }
            if ("actual".equals(situacion)) Animaciones.pulso(circulo);
        }
        if (!estado.cola().isBlank()) {
            Label cola = new Label("Estructura: " + estado.cola());
            cola.relocate(12, alto - 28);
            lienzo.getChildren().add(cola);
        }
        lienzo.setMinSize(ancho, alto);
        lienzo.setPrefSize(ancho, alto);
        return lienzo;
    }

    private Node dibujarTorres(EstadoVisual.Torres estado) {
        HBox varillas = new HBox(60);
        varillas.setAlignment(Pos.BOTTOM_CENTER);
        String[] nombres = {"A", "B", "C"};
        for (int v = 0; v < estado.varillas().size(); v++) {
            VBox pila = new VBox(2);
            pila.setAlignment(Pos.BOTTOM_CENTER);
            pila.setMinHeight(estado.discos() * 26 + 14);
            pila.setPrefWidth(60 + estado.discos() * 30);
            pila.getStyleClass().add("varilla");
            List<Integer> discos = estado.varillas().get(v);
            for (int d = discos.size() - 1; d >= 0; d--) {
                Region disco = new Region();
                disco.setMinSize(26 + discos.get(d) * 28, 24);
                disco.setPrefSize(26 + discos.get(d) * 28, 24);
                disco.setMaxSize(26 + discos.get(d) * 28, 24);
                disco.getStyleClass().addAll("disco", "disco-" + ((discos.get(d) - 1) % 6));
                pila.getChildren().add(disco);
            }
            Label nombre = new Label("Varilla " + nombres[v]);
            VBox columna = new VBox(8, pila, nombre);
            columna.setAlignment(Pos.BOTTOM_CENTER);
            varillas.getChildren().add(columna);
        }
        return varillas;
    }
}
