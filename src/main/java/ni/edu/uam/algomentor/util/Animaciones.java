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
