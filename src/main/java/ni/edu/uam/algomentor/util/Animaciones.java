package ni.edu.uam.algomentor.util;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public final class Animaciones {

    public enum Estado { NEUTRO, CORRECTO, ERROR }

    private static final String CLAVE_ANIMACION = "algomentor.animacion";
    private static final String CLAVE_ESCALA_BOTON = "algomentor.escalaBoton";
    private static final String CLAVE_ELEVACION = "algomentor.elevacion";
    private static final Interpolator SUAVE = Interpolator.SPLINE(0.16, 1, 0.3, 1);
    private static final Interpolator REBOTE = new Interpolator() {
        @Override
        protected double curve(double t) {
            double c1 = 1.70158;
            double c3 = c1 + 1;
            double u = t - 1;
            return 1 + c3 * u * u * u + c1 * u * u;
        }
    };
    private static final double PASO_ESCALONADO_MS = 70;
    private static final double RETRASO_MAXIMO_MS = 420;

    private Animaciones() {
    }

    public static void entrar(Node nodo, double retrasoMs, double desdeY) {
        reiniciar(nodo);
        nodo.setOpacity(0);
        nodo.setTranslateY(desdeY);
        FadeTransition aparecer = new FadeTransition(Duration.millis(320), nodo);
        aparecer.setToValue(1);
        TranslateTransition subir = new TranslateTransition(Duration.millis(420), nodo);
        subir.setToY(0);
        subir.setInterpolator(SUAVE);
        ParallelTransition entrada = new ParallelTransition(aparecer, subir);
        entrada.setDelay(Duration.millis(retrasoMs));
        reproducir(nodo, entrada);
    }

    public static void entrarEscalonado(List<? extends Node> nodos, double desdeY) {
        for (int i = 0; i < nodos.size(); i++) {
            entrar(nodos.get(i), Math.min(i * PASO_ESCALONADO_MS, RETRASO_MAXIMO_MS), desdeY);
        }
    }

    public static void aparecerConRebote(Node nodo, double retrasoMs) {
        reiniciar(nodo);
        nodo.setOpacity(0);
        nodo.setScaleX(0.5);
        nodo.setScaleY(0.5);
        FadeTransition aparecer = new FadeTransition(Duration.millis(220), nodo);
        aparecer.setToValue(1);
        ScaleTransition crecer = new ScaleTransition(Duration.millis(420), nodo);
        crecer.setToX(1);
        crecer.setToY(1);
        crecer.setInterpolator(REBOTE);
        ParallelTransition entrada = new ParallelTransition(aparecer, crecer);
        entrada.setDelay(Duration.millis(retrasoMs));
        reproducir(nodo, entrada);
    }

    public static void pulso(Node nodo) {
        reiniciar(nodo);
        ScaleTransition latido = new ScaleTransition(Duration.millis(170), nodo);
        latido.setToX(1.12);
        latido.setToY(1.12);
        latido.setCycleCount(2);
        latido.setAutoReverse(true);
        reproducir(nodo, latido);
    }

    public static void deslizarDesde(Node nodo, double desplazamientoX) {
        reiniciar(nodo);
        nodo.setTranslateX(desplazamientoX);
        nodo.setViewOrder(-1);
        TranslateTransition deslizar = new TranslateTransition(Duration.millis(480), nodo);
        deslizar.setToX(0);
        deslizar.setInterpolator(SUAVE);
        deslizar.setOnFinished(evento -> nodo.setViewOrder(0));
        reproducir(nodo, deslizar);
    }

    public static void sacudir(Node nodo) {
        reiniciar(nodo);
        Timeline temblor = new Timeline();
        double[] amplitudes = {-9, 9, -6, 6, -3, 0};
        for (int i = 0; i < amplitudes.length; i++) {
            temblor.getKeyFrames().add(new KeyFrame(Duration.millis(60.0 * (i + 1)),
                    new KeyValue(nodo.translateXProperty(), amplitudes[i], Interpolator.EASE_BOTH)));
        }
        reproducir(nodo, temblor);
    }

    public static void animarProgreso(ProgressBar barra, double hasta) {
        reiniciar(barra);
        barra.setProgress(0);
        Timeline llenado = new Timeline(new KeyFrame(Duration.millis(700),
                new KeyValue(barra.progressProperty(), hasta, SUAVE)));
        reproducir(barra, llenado);
    }

    public static void mostrarResultado(Label etiqueta, String texto, Estado estado) {
        etiqueta.setText(texto);
        etiqueta.getStyleClass().removeAll("resultado-ok", "resultado-error");
        switch (estado) {
            case CORRECTO -> {
                etiqueta.getStyleClass().add("resultado-ok");
                entrar(etiqueta, 0, 8);
            }
            case ERROR -> {
                etiqueta.getStyleClass().add("resultado-error");
                sacudir(etiqueta);
            }
            case NEUTRO -> reiniciar(etiqueta);
        }
    }

    public static void instalarEfectosBoton(Scene escena) {
        escena.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, evento -> {
            if (evento.getTarget() instanceof Button boton && !boton.isDisabled()) {
                escalarBoton(boton, 1.04, 140);
            }
        });
        escena.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, evento -> {
            if (evento.getTarget() instanceof Button boton) {
                escalarBoton(boton, 1, 180);
            }
        });
        escena.addEventFilter(MouseEvent.MOUSE_PRESSED, evento -> {
            Button boton = botonDe(evento.getTarget());
            if (boton != null && !boton.isDisabled()) {
                escalarBoton(boton, 0.95, 90);
            }
        });
        escena.addEventFilter(MouseEvent.MOUSE_RELEASED, evento -> {
            Button boton = botonDe(evento.getTarget());
            if (boton != null) {
                escalarBoton(boton, boton.isHover() ? 1.04 : 1, 140);
            }
        });
    }

    /** Cuenta desde el valor mostrado hasta el nuevo; si no cambió, no anima (evita reinicios al refrescar). */
    public static void contar(Label etiqueta, long hasta) {
        long desde = 0;
        try {
            desde = Long.parseLong(etiqueta.getText().trim());
        } catch (NumberFormatException e) {
            // Texto inicial (por ejemplo "—"): se cuenta desde cero.
        }
        if (desde == hasta && etiqueta.getText().trim().equals(Long.toString(hasta))) {
            return;
        }
        reiniciar(etiqueta);
        DoubleProperty valor = new SimpleDoubleProperty(desde);
        valor.addListener((obs, antes, ahora) -> etiqueta.setText(Long.toString(Math.round(ahora.doubleValue()))));
        etiqueta.setText(Long.toString(desde));
        Timeline conteo = new Timeline(new KeyFrame(Duration.millis(900), new KeyValue(valor, hasta, SUAVE)));
        conteo.setOnFinished(evento -> etiqueta.setText(Long.toString(hasta)));
        reproducir(etiqueta, conteo);
    }

    /** Orbes de luz que flotan lentamente detrás del contenido de un {@link StackPane}. */
    public static void fondoAnimado(StackPane raiz) {
        Pane capa = new Pane();
        capa.setMouseTransparent(true);
        capa.setPickOnBounds(false);
        capa.setManaged(false);
        raiz.getChildren().add(0, capa);
        capa.prefWidthProperty().bind(raiz.widthProperty());
        capa.prefHeightProperty().bind(raiz.heightProperty());
        capa.resize(raiz.getWidth(), raiz.getHeight());
        capa.setClip(clip(capa));

        Color[] colores = {Color.web("#22d3ee"), Color.web("#8b5cf6"), Color.web("#34d399"), Color.web("#6366f1")};
        double[][] posicion = {{0.18, 0.25}, {0.82, 0.30}, {0.30, 0.80}, {0.75, 0.78}};
        double[] radios = {170, 210, 140, 190};
        for (int i = 0; i < colores.length; i++) {
            Circle orbe = new Circle(radios[i]);
            orbe.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                    new Stop(0, colores[i].deriveColor(0, 1, 1, 0.20)),
                    new Stop(1, colores[i].deriveColor(0, 1, 1, 0))));
            orbe.setEffect(new BoxBlur(40, 40, 2));
            orbe.centerXProperty().bind(capa.widthProperty().multiply(posicion[i][0]));
            orbe.centerYProperty().bind(capa.heightProperty().multiply(posicion[i][1]));
            capa.getChildren().add(orbe);

            TranslateTransition flotar = new TranslateTransition(Duration.seconds(7 + i * 1.7), orbe);
            flotar.setByX(i % 2 == 0 ? 90 : -90);
            flotar.setByY(i < 2 ? 60 : -60);
            flotar.setAutoReverse(true);
            flotar.setCycleCount(Animation.INDEFINITE);
            flotar.setInterpolator(Interpolator.EASE_BOTH);
            flotar.play();

            ScaleTransition respirar = new ScaleTransition(Duration.seconds(4 + i), orbe);
            respirar.setToX(1.25);
            respirar.setToY(1.25);
            respirar.setAutoReverse(true);
            respirar.setCycleCount(Animation.INDEFINITE);
            respirar.setInterpolator(Interpolator.EASE_BOTH);
            respirar.play();
        }
        StackPane.setAlignment(capa, Pos.TOP_LEFT);
    }

    private static Rectangle clip(Pane capa) {
        Rectangle recorte = new Rectangle();
        recorte.widthProperty().bind(capa.widthProperty());
        recorte.heightProperty().bind(capa.heightProperty());
        return recorte;
    }

    /** Entrada del cuadro de inicio de sesión: escala, desvanece y revela sus hijos en cascada. */
    public static void entrarTarjetaLogin(Node tarjeta) {
        reiniciar(tarjeta);
        tarjeta.setOpacity(0);
        tarjeta.setScaleX(0.92);
        tarjeta.setScaleY(0.92);
        tarjeta.setTranslateY(28);
        FadeTransition aparecer = new FadeTransition(Duration.millis(450), tarjeta);
        aparecer.setToValue(1);
        ScaleTransition crecer = new ScaleTransition(Duration.millis(650), tarjeta);
        crecer.setToX(1);
        crecer.setToY(1);
        crecer.setInterpolator(REBOTE);
        TranslateTransition subir = new TranslateTransition(Duration.millis(600), tarjeta);
        subir.setToY(0);
        subir.setInterpolator(SUAVE);
        reproducir(tarjeta, new ParallelTransition(aparecer, crecer, subir));
        if (tarjeta instanceof Parent padre) {
            List<Node> hijos = new ArrayList<>(padre.getChildrenUnmodifiable());
            for (int i = 0; i < hijos.size(); i++) {
                Node hijo = hijos.get(i);
                hijo.setOpacity(0);
                hijo.setTranslateY(14);
                FadeTransition f = new FadeTransition(Duration.millis(360), hijo);
                f.setToValue(1);
                TranslateTransition t = new TranslateTransition(Duration.millis(420), hijo);
                t.setToY(0);
                t.setInterpolator(SUAVE);
                ParallelTransition par = new ParallelTransition(f, t);
                par.setDelay(Duration.millis(220 + i * 110));
                par.play();
            }
        }
    }

    /** Desvanecimiento suave al cambiar de pantalla completa (por ejemplo, tras iniciar sesión). */
    public static void fundirEntrada(Node raiz) {
        raiz.setOpacity(0);
        FadeTransition aparecer = new FadeTransition(Duration.millis(450), raiz);
        aparecer.setToValue(1);
        aparecer.play();
    }

    /** Las tarjetas pequeñas de resumen se elevan un poco al pasar el cursor; las grandes no se mueven. */
    public static void instalarEfectosTarjeta(Scene escena) {
        escena.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, evento -> {
            Node tarjeta = tarjetaDe(evento.getTarget());
            if (tarjeta != null && evento.getTarget() == tarjeta) {
                elevar(tarjeta, -4, 1.012);
            }
        });
        escena.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, evento -> {
            Node tarjeta = tarjetaDe(evento.getTarget());
            if (tarjeta != null && evento.getTarget() == tarjeta) {
                elevar(tarjeta, 0, 1);
            }
        });
    }

    /** Latido de opacidad continuo, por ejemplo para etiquetas de carga. Devuelve la animación para poder detenerla. */
    public static Animation parpadear(Node nodo) {
        FadeTransition fundido = new FadeTransition(Duration.millis(700), nodo);
        fundido.setFromValue(1);
        fundido.setToValue(0.35);
        fundido.setAutoReverse(true);
        fundido.setCycleCount(Animation.INDEFINITE);
        fundido.play();
        return fundido;
    }

    private static Node tarjetaDe(Object objetivo) {
        if (objetivo instanceof Node nodo && nodo.getStyleClass().contains("summary-card")) {
            return nodo;
        }
        return null;
    }

    private static void elevar(Node tarjeta, double y, double escala) {
        if (tarjeta.getProperties().get(CLAVE_ELEVACION) instanceof Animation previa) {
            previa.stop();
        }
        TranslateTransition subir = new TranslateTransition(Duration.millis(220), tarjeta);
        subir.setToY(y);
        subir.setInterpolator(SUAVE);
        ScaleTransition crecer = new ScaleTransition(Duration.millis(220), tarjeta);
        crecer.setToX(escala);
        crecer.setToY(escala);
        crecer.setInterpolator(SUAVE);
        ParallelTransition elevacion = new ParallelTransition(subir, crecer);
        tarjeta.getProperties().put(CLAVE_ELEVACION, elevacion);
        elevacion.play();
    }

    public static void animarPestanas(TabPane panel) {
        panel.getSelectionModel().selectedItemProperty().addListener((obs, anterior, tab) -> {
            if (tab != null && tab.getContent() != null) {
                Node contenido = tab.getContent();
                Platform.runLater(() -> entrarContenido(contenido));
            }
        });
    }

    public static void entrarContenido(Node raiz) {
        if (raiz.getScene() != null && raiz instanceof Parent padre) {
            padre.applyCss();
        }
        List<Node> encabezados = buscar(raiz, ".page-header");
        List<Node> tarjetas = buscar(raiz, ".card");
        entrarEscalonado(encabezados, -14);
        if (tarjetas.isEmpty()) {
            entrar(raiz, 0, 10);
        } else {
            entrarEscalonado(tarjetas, 18);
        }
    }

    private static List<Node> buscar(Node raiz, String selector) {
        return new ArrayList<>(raiz.lookupAll(selector));
    }

    private static Button botonDe(Object objetivo) {
        Node actual = objetivo instanceof Node nodo ? nodo : null;
        while (actual != null && !(actual instanceof Button)) {
            actual = actual.getParent();
        }
        return (Button) actual;
    }

    private static void escalarBoton(Button boton, double escala, double ms) {
        if (boton.getProperties().get(CLAVE_ESCALA_BOTON) instanceof Animation previa) {
            previa.stop();
        }
        ScaleTransition transicion = new ScaleTransition(Duration.millis(ms), boton);
        transicion.setToX(escala);
        transicion.setToY(escala);
        transicion.setInterpolator(Interpolator.EASE_OUT);
        boton.getProperties().put(CLAVE_ESCALA_BOTON, transicion);
        transicion.play();
    }

    private static void reiniciar(Node nodo) {
        if (nodo.getProperties().remove(CLAVE_ANIMACION) instanceof Animation previa) {
            previa.stop();
        }
        nodo.setOpacity(1);
        nodo.setTranslateX(0);
        nodo.setTranslateY(0);
        nodo.setScaleX(1);
        nodo.setScaleY(1);
        nodo.setViewOrder(0);
    }

    private static void reproducir(Node nodo, Animation animacion) {
        nodo.getProperties().put(CLAVE_ANIMACION, animacion);
        animacion.play();
    }
}
