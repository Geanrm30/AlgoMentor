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

import javafx.scene.effect.DropShadow;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Scale;

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

    /** Fondo del inicio de sesión: la malla de siempre con cintas brillantes que derivan despacio. */
    public static void instalarLogin(StackPane contenedor, boolean claro) {
        contenedor.getChildren().removeIf(nodo -> nodo.getStyleClass().contains(CLASE));
        Pane capa = malla(claro, false);
        capa.setCache(false);
        Group cintas = new Group();
        Scale escala = new Scale();
        escala.xProperty().bind(capa.widthProperty().divide(1600));
        escala.yProperty().bind(capa.heightProperty().divide(900));
        cintas.getTransforms().add(escala);
        double[][] trazos = {
                {-100, 700, 300, 420, 700, 900, 1100, 560, 1700, 330, 120},
                {-100, 520, 350, 260, 800, 700, 1300, 380, 1700, 180, 80},
                {-100, 880, 500, 760, 900, 500, 1200, 760, 1700, 640, 60}};
        String[] colores = claro ? new String[]{"#818cf8", "#38bdf8", "#c084fc"} : new String[]{"#22d3ee", "#6366f1", "#8b5cf6"};
        for (int i = 0; i < trazos.length; i++) {
            double[] t = trazos[i];
            Path cinta = new Path(new MoveTo(t[0], t[1]), new CubicCurveTo(t[2], t[3], t[4], t[5], t[6], t[7]),
                    new CubicCurveTo(t[8] - 300, t[7] + 100, t[8] - 150, t[9], t[8], t[9]));
            cinta.setFill(null);
            cinta.setStrokeLineCap(StrokeLineCap.ROUND);
            Color base = Color.web(colores[i]);
            cinta.setStroke(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, base.deriveColor(0, 1, 1, 0)), new Stop(0.5, base.deriveColor(0, 1, 1, claro ? 0.28 : 0.30)),
                    new Stop(1, base.deriveColor(0, 1, 1, 0))));
            cinta.setStrokeWidth(t[10]);
            cinta.setEffect(new BoxBlur(26, 26, 2));
            Path borde = new Path(new MoveTo(t[0], t[1]), new CubicCurveTo(t[2], t[3], t[4], t[5], t[6], t[7]),
                    new CubicCurveTo(t[8] - 300, t[7] + 100, t[8] - 150, t[9], t[8], t[9]));
            borde.setFill(null);
            borde.setStrokeWidth(2);
            borde.setStroke(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, base.deriveColor(0, 1, 1, 0)), new Stop(0.5, base.deriveColor(0, 1, 1, claro ? 0.55 : 0.85)),
                    new Stop(1, base.deriveColor(0, 1, 1, 0))));
            borde.setEffect(new DropShadow(14, base.deriveColor(0, 1, 1, 0.8)));
            Group grupo = new Group(cinta, borde);
            grupo.setCache(true);
            grupo.setCacheHint(CacheHint.SPEED);
            TranslateTransition derivar = new TranslateTransition(Duration.seconds(11 + i * 3), grupo);
            derivar.setByX(i % 2 == 0 ? 60 : -60);
            derivar.setByY(i % 2 == 0 ? -34 : 30);
            derivar.setAutoReverse(true);
            derivar.setCycleCount(Animation.INDEFINITE);
            derivar.setInterpolator(Interpolator.EASE_BOTH);
            derivar.play();
            cintas.getChildren().add(grupo);
        }
        capa.getChildren().add(1, cintas);
        contenedor.getChildren().add(0, capa);
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
