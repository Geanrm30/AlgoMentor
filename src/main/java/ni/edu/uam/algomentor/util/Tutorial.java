package ni.edu.uam.algomentor.util;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Supplier;

/**
 * Recorrido guiado: oscurece la ventana, ilumina un elemento a la vez y explica para qué sirve.
 * Se muestra la primera vez que se entra y se puede repetir desde Ayuda.
 */
public final class Tutorial {

    /** Un paso: texto, elemento a resaltar (se calcula al llegar) y una acción previa, por ejemplo abrir la sección. */
    public record Paso(String titulo, String texto, Supplier<Node> objetivo, Runnable antes) {
        public Paso(String titulo, String texto, Supplier<Node> objetivo) {
            this(titulo, texto, objetivo, null);
        }
    }

    private static final double MARGEN = 10;
    private static final double ANCHO_TARJETA = 410;
    private static final double SEPARACION = 18;
    private static final double BORDE_VENTANA = 12;

    private final StackPane contenedor;
    private final List<Paso> pasos;
    private final Runnable alTerminar;
    private final Pane capa = new Pane();
    private final Rectangle hueco = new Rectangle();
    private final Shape[] oscuro = new Shape[1];
    private final Rectangle borde = new Rectangle();
    private final VBox tarjeta = new VBox(8);
    private final Label lblNumero = new Label();
    private final Label lblTitulo = new Label();
    private final Label lblTexto = new Label();
    private final Button btnOmitir = new Button("Omitir");
    private final Button btnAnterior = new Button("Anterior");
    private final Button btnSiguiente = new Button("Siguiente");
    private final EventHandler<KeyEvent> teclas = this::teclado;
    private Scene escena;
    private Timeline movimiento;
    private int indice;
    private int pasoVigente;
    private boolean activo;

    public Tutorial(StackPane contenedor, List<Paso> pasos, Runnable alTerminar) {
        if (pasos == null || pasos.isEmpty()) {
            throw new IllegalArgumentException("El tutorial necesita al menos un paso.");
        }
        this.contenedor = contenedor;
        this.pasos = List.copyOf(pasos);
        this.alTerminar = alTerminar == null ? () -> { } : alTerminar;
        construir();
    }

    public boolean activo() {
        return activo;
    }

    public void iniciar() {
        if (activo) {
            return;
        }
        activo = true;
        escena = contenedor.getScene();
        capa.setOpacity(0);
        contenedor.getChildren().add(capa);
        if (escena != null) {
            escena.addEventFilter(KeyEvent.KEY_PRESSED, teclas);
        }
        FadeTransition aparecer = new FadeTransition(Duration.millis(260), capa);
        aparecer.setToValue(1);
        aparecer.play();
        ir(0, false);
    }

    public void terminar() {
        if (!activo) {
            return;
        }
        activo = false;
        pasoVigente++;
        if (movimiento != null) {
            movimiento.stop();
        }
        if (escena != null) {
            escena.removeEventFilter(KeyEvent.KEY_PRESSED, teclas);
        }
        FadeTransition salir = new FadeTransition(Duration.millis(200), capa);
        salir.setToValue(0);
        salir.setOnFinished(evento -> contenedor.getChildren().remove(capa));
        salir.play();
        alTerminar.run();
    }

    /**
     * Coloca la tarjeta a la derecha del hueco; si no cabe, a la izquierda, debajo o encima, y siempre dentro
     * de la ventana. Es independiente de JavaFX para poder probarla.
     */
    public static Point2D ubicarTarjeta(Bounds hueco, double ancho, double alto, double anchoVentana, double altoVentana) {
        double x;
        double y;
        if (hueco.getMaxX() + SEPARACION + ancho <= anchoVentana - BORDE_VENTANA) {
            x = hueco.getMaxX() + SEPARACION;
            y = hueco.getMinY();
        } else if (hueco.getMinX() - SEPARACION - ancho >= BORDE_VENTANA) {
            x = hueco.getMinX() - SEPARACION - ancho;
            y = hueco.getMinY();
        } else if (hueco.getMaxY() + SEPARACION + alto <= altoVentana - BORDE_VENTANA) {
            x = hueco.getMinX();
            y = hueco.getMaxY() + SEPARACION;
        } else {
            x = hueco.getMinX();
            y = hueco.getMinY() - SEPARACION - alto;
        }
        x = Math.max(BORDE_VENTANA, Math.min(x, anchoVentana - ancho - BORDE_VENTANA));
        y = Math.max(BORDE_VENTANA, Math.min(y, altoVentana - alto - BORDE_VENTANA));
        return new Point2D(x, y);
    }

    private void construir() {
        capa.setPickOnBounds(true);
        capa.prefWidthProperty().bind(contenedor.widthProperty());
        capa.prefHeightProperty().bind(contenedor.heightProperty());
        capa.setMinSize(0, 0);
        capa.setFocusTraversable(false);

        borde.setArcWidth(24);
        borde.setArcHeight(24);
        borde.setFill(Color.TRANSPARENT);
        borde.setStroke(Color.web("#22d3ee"));
        borde.setStrokeWidth(2.5);
        borde.setMouseTransparent(true);
        hueco.setArcWidth(24);
        hueco.setArcHeight(24);

        lblNumero.getStyleClass().add("tutorial-numero");
        lblTitulo.getStyleClass().add("tutorial-titulo");
        lblTexto.getStyleClass().add("tutorial-texto");
        lblTexto.setWrapText(true);
        lblTitulo.setWrapText(true);
        btnOmitir.setOnAction(evento -> terminar());
        btnAnterior.setOnAction(evento -> anterior());
        btnSiguiente.setOnAction(evento -> siguiente());
        btnSiguiente.getStyleClass().add("primary-button");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        for (Button boton : new Button[]{btnOmitir, btnAnterior, btnSiguiente}) {
            boton.setMinWidth(Region.USE_PREF_SIZE);
        }
        HBox fila = new HBox(6, btnOmitir, espacio, btnAnterior, btnSiguiente);
        fila.setAlignment(Pos.CENTER_RIGHT);
        tarjeta.getChildren().addAll(lblNumero, lblTitulo, lblTexto, fila);
        tarjeta.getStyleClass().addAll("card", "tutorial-card");
        tarjeta.setPrefWidth(ANCHO_TARJETA);
        tarjeta.setMinWidth(ANCHO_TARJETA);
        tarjeta.setMaxWidth(ANCHO_TARJETA);
        capa.getChildren().addAll(borde, tarjeta);

        hueco.xProperty().addListener((obs, a, b) -> actualizarOscuro());
        hueco.yProperty().addListener((obs, a, b) -> actualizarOscuro());
        hueco.widthProperty().addListener((obs, a, b) -> actualizarOscuro());
        hueco.heightProperty().addListener((obs, a, b) -> actualizarOscuro());
        capa.widthProperty().addListener((obs, a, b) -> actualizarOscuro());
        capa.heightProperty().addListener((obs, a, b) -> actualizarOscuro());
    }

    private void ir(int nuevo, boolean animar) {
        indice = nuevo;
        int vigente = ++pasoVigente;
        Paso paso = pasos.get(nuevo);
        lblNumero.setText("PASO " + (nuevo + 1) + " DE " + pasos.size());
        lblTitulo.setText(paso.titulo());
        lblTexto.setText(paso.texto());
        btnAnterior.setDisable(nuevo == 0);
        btnSiguiente.setText(nuevo == pasos.size() - 1 ? "Finalizar" : "Siguiente");
        if (paso.antes() != null) {
            paso.antes().run();
        }
        PauseTransition espera = new PauseTransition(Duration.millis(paso.antes() != null ? 320 : 40));
        espera.setOnFinished(evento -> {
            if (activo && vigente == pasoVigente) {
                ubicar(paso, animar);
            }
        });
        espera.play();
    }

    private void siguiente() {
        if (indice >= pasos.size() - 1) {
            terminar();
        } else {
            ir(indice + 1, true);
        }
    }

    private void anterior() {
        if (indice > 0) {
            ir(indice - 1, true);
        }
    }

    private void teclado(KeyEvent evento) {
        switch (evento.getCode()) {
            case ESCAPE -> terminar();
            case RIGHT, ENTER, SPACE -> siguiente();
            case LEFT -> anterior();
            default -> {
                return;
            }
        }
        evento.consume();
    }

    private void ubicar(Paso paso, boolean animar) {
        Bounds destino = limites(paso.objetivo().get());
        double ancho = capa.getWidth();
        double alto = capa.getHeight();
        if (animar && hueco.getWidth() > 0) {
            if (movimiento != null) {
                movimiento.stop();
            }
            movimiento = new Timeline(new KeyFrame(Duration.millis(340),
                    new KeyValue(hueco.xProperty(), destino.getMinX(), Interpolator.EASE_BOTH),
                    new KeyValue(hueco.yProperty(), destino.getMinY(), Interpolator.EASE_BOTH),
                    new KeyValue(hueco.widthProperty(), destino.getWidth(), Interpolator.EASE_BOTH),
                    new KeyValue(hueco.heightProperty(), destino.getHeight(), Interpolator.EASE_BOTH)));
            movimiento.play();
        } else {
            hueco.setX(destino.getMinX());
            hueco.setY(destino.getMinY());
            hueco.setWidth(destino.getWidth());
            hueco.setHeight(destino.getHeight());
        }
        tarjeta.applyCss();
        tarjeta.autosize();
        double altoTarjeta = tarjeta.prefHeight(ANCHO_TARJETA);
        tarjeta.resize(ANCHO_TARJETA, altoTarjeta);
        Point2D posicion = ubicarTarjeta(destino, ANCHO_TARJETA, altoTarjeta, ancho, alto);
        if (animar) {
            new Timeline(new KeyFrame(Duration.millis(340),
                    new KeyValue(tarjeta.layoutXProperty(), posicion.getX(), Interpolator.EASE_BOTH),
                    new KeyValue(tarjeta.layoutYProperty(), posicion.getY(), Interpolator.EASE_BOTH))).play();
        } else {
            tarjeta.relocate(posicion.getX(), posicion.getY());
        }
        borde.xProperty().bind(hueco.xProperty());
        borde.yProperty().bind(hueco.yProperty());
        borde.widthProperty().bind(hueco.widthProperty());
        borde.heightProperty().bind(hueco.heightProperty());
        actualizarOscuro();
    }

    /** Rectángulo del elemento con margen, relativo al contenedor. Usa la posición de diseño, sin transformaciones de animación. */
    private Bounds limites(Node objetivo) {
        if (objetivo == null || objetivo.getScene() == null) {
            return new BoundingBox(capa.getWidth() / 2 - 80, capa.getHeight() / 2 - 40, 160, 80);
        }
        Bounds diseno = objetivo.getLayoutBounds();
        Node padre = objetivo.getParent();
        Bounds enEscena = padre == null
                ? objetivo.localToScene(diseno)
                : padre.localToScene(new BoundingBox(objetivo.getLayoutX() + diseno.getMinX(),
                        objetivo.getLayoutY() + diseno.getMinY(), diseno.getWidth(), diseno.getHeight()));
        Bounds local = contenedor.sceneToLocal(enEscena);
        return new BoundingBox(local.getMinX() - MARGEN, local.getMinY() - MARGEN,
                local.getWidth() + 2 * MARGEN, local.getHeight() + 2 * MARGEN);
    }

    private void actualizarOscuro() {
        if (capa.getWidth() <= 0 || capa.getHeight() <= 0) {
            return;
        }
        capa.getChildren().remove(oscuro[0]);
        Rectangle fondo = new Rectangle(capa.getWidth(), capa.getHeight());
        Shape sombra = Shape.subtract(fondo, hueco);
        sombra.setFill(Color.rgb(0, 0, 0, 0.68));
        sombra.setStroke(null);
        capa.getChildren().add(0, sombra);
        oscuro[0] = sombra;
    }
}
