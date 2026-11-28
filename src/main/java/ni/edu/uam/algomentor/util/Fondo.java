package ni.edu.uam.algomentor.util;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Random;

/** Fondo de malla de gradiente (manchas de color difuminadas) con partículas tenues en el tema oscuro. */
public final class Fondo {

    private static final String CLASE = "fondo-malla";

    /** Posición (fracción del ancho y del alto), radio (fracción del lado mayor), color y opacidad de cada mancha. */
    private static final double[][] OSCURO = {
            {0.14, 0.18, 0.30, 0x7c3aed, 0.48}, {0.88, 0.14, 0.26, 0xdb2777, 0.32},
            {0.78, 0.88, 0.30, 0x0ea5a5, 0.32}, {0.10, 0.90, 0.28, 0x4f46e5, 0.42},
            {0.50, 0.50, 0.22, 0x9333ea, 0.24}};
    private static final double[][] CLARO = {
            {0.14, 0.18, 0.30, 0xc4b5fd, 0.60}, {0.88, 0.14, 0.26, 0xfda4af, 0.45},
            {0.78, 0.88, 0.30, 0x93c5fd, 0.55}, {0.10, 0.90, 0.28, 0x6ee7b7, 0.40},
            {0.50, 0.50, 0.22, 0xe9d5ff, 0.40}};

    private Fondo() { }

    /** Pone el fondo detrás de todo el contenido del contenedor, reemplazando el anterior si lo había. */
    public static void instalar(StackPane contenedor, boolean claro, boolean animado) {
        contenedor.getChildren().removeIf(nodo -> nodo.getStyleClass().contains(CLASE));
        contenedor.getChildren().add(0, malla(claro, animado));
    }

    public static Pane malla(boolean claro, boolean animado) {
        Pane capa = new Pane();
        capa.getStyleClass().add(CLASE);
        capa.setMouseTransparent(true);
        capa.setStyle("-fx-background-color: " + (claro ? "#eef0f8" : "#080c1a") + ";");

        Group manchas = new Group();
        double[][] datos = claro ? CLARO : OSCURO;
        for (int i = 0; i < datos.length; i++) {
            double[] d = datos[i];
            Color color = Color.web(String.format("#%06x", (int) d[3]));
            Circle mancha = new Circle();
            mancha.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                    new Stop(0, color.deriveColor(0, 1, 1, d[4])), new Stop(1, color.deriveColor(0, 1, 1, 0))));
            mancha.centerXProperty().bind(capa.widthProperty().multiply(d[0]));
            mancha.centerYProperty().bind(capa.heightProperty().multiply(d[1]));
            mancha.radiusProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.max(capa.getWidth(), capa.getHeight()) * d[2], capa.widthProperty(), capa.heightProperty()));
            manchas.getChildren().add(mancha);
            if (animado) {
                TranslateTransition flotar = new TranslateTransition(Duration.seconds(8 + i * 1.7), mancha);
                flotar.setByX(i % 2 == 0 ? 90 : -90);
                flotar.setByY(i < 3 ? 60 : -60);
                flotar.setAutoReverse(true);
                flotar.setCycleCount(Animation.INDEFINITE);
                flotar.setInterpolator(Interpolator.EASE_BOTH);
                flotar.play();
                ScaleTransition respirar = new ScaleTransition(Duration.seconds(5 + i), mancha);
                respirar.setToX(1.2);
                respirar.setToY(1.2);
                respirar.setAutoReverse(true);
                respirar.setCycleCount(Animation.INDEFINITE);
                respirar.setInterpolator(Interpolator.EASE_BOTH);
                respirar.play();
            }
        }
        manchas.setEffect(new BoxBlur(90, 90, 2));
        capa.getChildren().add(manchas);

        if (!claro) {
            Random azar = new Random(4);
            for (int i = 0; i < 60; i++) {
                double fx = azar.nextDouble();
                double fy = azar.nextDouble();
                Circle particula = new Circle(0.8 + azar.nextDouble() * 1.6);
                particula.setFill(Color.web("#c7d2fe", 0.15 + azar.nextDouble() * 0.4));
                particula.centerXProperty().bind(capa.widthProperty().multiply(fx));
                particula.centerYProperty().bind(capa.heightProperty().multiply(fy));
                capa.getChildren().add(particula);
            }
        }
        if (!animado) {
            // Sin animación, el desenfoque se calcula una sola vez y se reutiliza.
            capa.setCache(true);
            capa.setCacheHint(CacheHint.SPEED);
        }
        return capa;
    }
}
