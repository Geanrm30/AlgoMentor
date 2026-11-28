package ni.edu.uam.algomentor.util;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Formulario que sale de una pastilla como si fuera líquido: los campos brotan del botón, se separan con un cuello
 * que se estira y se afina, y al enviar se funden otra vez en la pastilla, que se encoge a un círculo con un giro
 * mientras se espera y termina con un check. Todo el movimiento depende de un único progreso (0 cerrado, 1 abierto).
 */
public final class FormularioLiquido extends Pane {

    public static final double ALTO_PASTILLA = 54;
    public static final double ESPACIO = 14;

    /** Posición vertical de cada fila, de la pastilla y alto total con el formulario abierto. */
    public record Disposicion(double[] y, double yPastilla, double total) { }

    private static final String CLAVE_GIRO = "algomentor.giroPastilla";

    private final double ancho;
    private final Button pastilla;
    private final DoubleProperty progreso = new SimpleDoubleProperty(0);
    private final DoubleProperty anchoPastilla = new SimpleDoubleProperty();
    private final DoubleProperty alturaTotal = new SimpleDoubleProperty(ALTO_PASTILLA);
    private final List<Region> filas = new ArrayList<>();
    private final List<Region> cuellos = new ArrayList<>();
    private Disposicion disposicion = disponer(new double[0]);
    private Timeline movimiento;
    private Timeline cambioPastilla;
    private PauseTransition comerTexto;
    private boolean abierto;

    public FormularioLiquido(double ancho, Button pastilla) {
        this.ancho = ancho;
        this.pastilla = pastilla;
        setMinWidth(ancho);
        setPrefWidth(ancho);
        setMaxWidth(ancho);
        pastilla.getStyleClass().add("pastilla");
        pastilla.setMinHeight(ALTO_PASTILLA);
        pastilla.setPrefHeight(ALTO_PASTILLA);
        pastilla.setMaxHeight(ALTO_PASTILLA);
        anchoPastilla.addListener((obs, antes, ahora) -> {
            double w = ahora.doubleValue();
            pastilla.setMinWidth(w);
            pastilla.setPrefWidth(w);
            pastilla.setMaxWidth(w);
            pastilla.setLayoutX((ancho - w) / 2);
        });
        anchoPastilla.set(ancho);
        progreso.addListener((obs, antes, ahora) -> aplicar(ahora.doubleValue()));
        getChildren().add(pastilla);
        aplicar(0);
    }

    // ---------- Cálculos puros (se prueban sin JavaFX) ----------

    /** Coloca las filas una bajo otra con {@link #ESPACIO} entre ellas y la pastilla al final. */
    public static Disposicion disponer(double[] alturas) {
        double[] y = new double[alturas.length];
        double siguiente = 0;
        for (int i = 0; i < alturas.length; i++) {
            y[i] = siguiente;
            siguiente += alturas[i] + ESPACIO;
        }
        return new Disposicion(y, siguiente, siguiente + ALTO_PASTILLA);
    }

    /** Curva de resorte: sale de 0, se pasa un poco de 1 y se asienta, como un líquido que rebota. */
    public static double resorte(double t) {
        if (t <= 0) return 0;
        if (t >= 1) return 1;
        return 1 - Math.exp(-5.5 * t) * Math.cos(9.5 * t);
    }

    // ---------- Contenido ----------

    /** Cambia las filas del formulario (por ejemplo, de inicio de sesión a registro); quedan ocultas hasta abrir. */
    public void definir(List<? extends Region> nuevas) {
        getChildren().removeAll(filas);
        getChildren().removeAll(cuellos);
        getChildren().remove(pastilla);
        filas.clear();
        cuellos.clear();
        double[] alturas = new double[nuevas.size()];
        for (int i = 0; i < nuevas.size(); i++) {
            Region cuello = new Region();
            cuello.getStyleClass().add("cuello-liquido");
            cuello.setMouseTransparent(true);
            cuello.setVisible(false);
            cuellos.add(cuello);
            filas.add(nuevas.get(i));
            alturas[i] = nuevas.get(i).getPrefHeight();
        }
        getChildren().addAll(cuellos);
        getChildren().addAll(filas);
        getChildren().add(pastilla);
        disposicion = disponer(alturas);
        alturaTotal.set(disposicion.total());
        aplicar(progreso.get());
    }

    /** Alto que tiene el formulario con todos los campos abiertos; sirve para anclar lo que lo contiene. */
    public DoubleProperty alturaTotalProperty() {
        return alturaTotal;
    }

    public boolean abierto() {
        return abierto;
    }

    /** Hace brotar los campos de la pastilla con rebote. */
    public void abrir(Runnable alTerminar) {
        abierto = true;
        animar(1, Duration.millis(820), true, alTerminar);
    }

    /** Funde los campos de vuelta en la pastilla. */
    public void cerrar(Runnable alTerminar) {
        abierto = false;
        animar(0, Duration.millis(520), false, alTerminar);
    }

    private void animar(double destino, Duration duracion, boolean conResorte, Runnable alTerminar) {
        if (movimiento != null) {
            movimiento.stop();
        }
        Interpolator curva = conResorte ? new Interpolator() {
            @Override protected double curve(double t) { return resorte(t); }
        } : Interpolator.EASE_BOTH;
        movimiento = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progreso, progreso.get())),
                new KeyFrame(duracion, new KeyValue(progreso, destino, curva)));
        movimiento.setOnFinished(evento -> {
            progreso.set(destino);
            if (alTerminar != null) {
                alTerminar.run();
            }
        });
        movimiento.play();
    }

    /** Coloca todo según el progreso: filas, cuellos, pastilla y alto del contenedor. */
    private void aplicar(double p) {
        double pc = Math.max(0, Math.min(1, p));
        // Con el rebote el alto también se pasa un poco, para que la pastilla no invada lo que hay debajo.
        double alto = ALTO_PASTILLA + (disposicion.total() - ALTO_PASTILLA) * Math.max(0, p);
        setMinHeight(alto);
        setPrefHeight(alto);
        setMaxHeight(alto);
        pastilla.setLayoutY(disposicion.yPastilla() * p);
        for (int i = 0; i < filas.size(); i++) {
            Region fila = filas.get(i);
            double altoFila = fila.getPrefHeight();
            double centroFinal = disposicion.y()[i] + altoFila / 2;
            fila.setLayoutX(0);
            fila.setLayoutY(disposicion.y()[i]);
            fila.setTranslateY((1 - p) * (ALTO_PASTILLA / 2 - centroFinal));
            fila.setOpacity(Math.max(0, Math.min(1, p * 2.2)));
            fila.setScaleX(0.82 + 0.18 * Math.max(0, Math.min(1.1, p)));
            fila.setScaleY(0.35 + 0.65 * Math.max(0, Math.min(1.1, p)));
            fila.setVisible(p > 0.002);
        }
        // Segunda pasada: ya están todas las filas colocadas, así que se puede medir el hueco entre cada par.
        for (int i = 0; i < filas.size(); i++) {
            Region fila = filas.get(i);
            double altoFila = fila.getPrefHeight();
            // El cuello solo puentea el hueco entre esta fila y la siguiente (o la pastilla), nunca cruza un campo.
            double mitad = altoFila * fila.getScaleY() / 2;
            double centro = fila.getLayoutY() + altoFila / 2 + fila.getTranslateY();
            double base = centro + mitad;
            double siguienteTop;
            if (i + 1 < filas.size()) {
                Region sig = filas.get(i + 1);
                siguienteTop = sig.getLayoutY() + sig.getPrefHeight() / 2 + sig.getTranslateY()
                        - sig.getPrefHeight() * sig.getScaleY() / 2;
            } else {
                siguienteTop = pastilla.getLayoutY();
            }
            Region cuello = cuellos.get(i);
            double grosor = this.ancho * 0.28 * Math.sin(Math.PI * pc);
            double hueco = siguienteTop - base;
            cuello.setVisible(grosor > 1 && hueco > 1 && fila.isVisible());
            cuello.setLayoutX((this.ancho - grosor) / 2);
            cuello.setLayoutY(base);
            cuello.setMinSize(grosor, Math.max(0, hueco));
            cuello.setPrefSize(grosor, Math.max(0, hueco));
            cuello.setMaxSize(grosor, Math.max(0, hueco));
        }
    }

    /** Pone el foco en el primer campo de texto visible. */
    public void enfocarPrimero() {
        for (Region fila : filas) {
            Node campo = fila.lookup(".campo-login-texto");
            if (campo instanceof TextInputControl && campo.isVisible()) {
                campo.requestFocus();
                return;
            }
        }
    }

    // ---------- Estados de la pastilla ----------

    private void animarAncho(double destino, double ms, Runnable alTerminar) {
        if (cambioPastilla != null) {
            cambioPastilla.stop();
        }
        cambioPastilla = new Timeline(new KeyFrame(Duration.millis(ms),
                new KeyValue(anchoPastilla, destino, Interpolator.EASE_BOTH)));
        cambioPastilla.setOnFinished(evento -> {
            if (alTerminar != null) {
                alTerminar.run();
            }
        });
        cambioPastilla.play();
    }

    private void detenerGiro() {
        if (comerTexto != null) {
            comerTexto.stop();
            comerTexto = null;
        }
        if (pastilla.getProperties().remove(CLAVE_GIRO) instanceof Animation giro) {
            giro.stop();
        }
    }

    /** El texto es "comido" por un ícono: la pastilla se encoge a un círculo con un giro mientras se espera. */
    public void pastillaOcupada() {
        detenerGiro();
        Region giroIcono = Iconos.crear("actualizar");
        pastilla.setGraphic(giroIcono);
        RotateTransition giro = new RotateTransition(Duration.millis(900), giroIcono);
        giro.setByAngle(360);
        giro.setCycleCount(Animation.INDEFINITE);
        giro.setInterpolator(Interpolator.LINEAR);
        pastilla.getProperties().put(CLAVE_GIRO, giro);
        giro.play();
        comerTexto = new PauseTransition(Duration.millis(110));
        comerTexto.setOnFinished(evento -> {
            if (pastilla.getGraphic() != null) {
                pastilla.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        });
        comerTexto.play();
        pastilla.setDisable(false);
        animarAncho(ALTO_PASTILLA, 280, null);
    }

    /** La pastilla se ensancha otra vez con un check y el texto dado, y avisa pasado un instante. */
    public void pastillaListo(String texto, Runnable alTerminar) {
        detenerGiro();
        pastilla.getStyleClass().remove("pastilla-exito");
        pastilla.getStyleClass().add("pastilla-exito");
        pastilla.setGraphic(Iconos.crear("check"));
        pastilla.setText(texto);
        pastilla.setContentDisplay(ContentDisplay.LEFT);
        pastilla.setGraphicTextGap(10);
        animarAncho(ancho, 360, () -> {
            PauseTransition espera = new PauseTransition(Duration.millis(450));
            espera.setOnFinished(evento -> {
                if (alTerminar != null) {
                    alTerminar.run();
                }
            });
            espera.play();
        });
    }

    /** La pastilla se pone roja un instante para indicar que algo salió mal. */
    public void destacarError() {
        pastilla.getStyleClass().remove("pastilla-error");
        pastilla.getStyleClass().add("pastilla-error");
        PauseTransition quitar = new PauseTransition(Duration.millis(1400));
        quitar.setOnFinished(evento -> pastilla.getStyleClass().remove("pastilla-error"));
        quitar.play();
    }

    /** Vuelve la pastilla a su forma normal con el texto dado. */
    public void pastillaNormal(String texto) {
        detenerGiro();
        pastilla.getStyleClass().remove("pastilla-exito");
        pastilla.setGraphic(null);
        pastilla.setText(texto);
        pastilla.setContentDisplay(ContentDisplay.TEXT_ONLY);
        animarAncho(ancho, 300, null);
    }
}
