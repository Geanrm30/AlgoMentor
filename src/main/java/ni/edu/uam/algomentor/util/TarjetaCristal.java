package ni.edu.uam.algomentor.util;

import javafx.beans.InvalidationListener;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;

import java.util.List;

/**
 * Convierte un {@link StackPane} en una tarjeta de vidrio con dos esquinas cortadas en diagonal (arriba a la izquierda y
 * abajo a la derecha) y dos esquinas redondeadas. El contorno se dibuja detrás del contenido y sigue el tamaño de la tarjeta.
 */
public final class TarjetaCristal {

    private static final double RADIO = 16;

    private TarjetaCristal() { }

    /** Forma de la tarjeta para un ancho y un alto dados; es pura para poder probarla. */
    public static List<double[]> puntos(double ancho, double alto, double corte) {
        return List.of(
                new double[]{corte, 0}, new double[]{ancho - RADIO, 0}, new double[]{ancho, RADIO},
                new double[]{ancho, alto - corte}, new double[]{ancho - corte, alto}, new double[]{RADIO, alto},
                new double[]{0, alto - RADIO}, new double[]{0, corte});
    }

    public static void instalar(StackPane tarjeta, double corte) {
        Path fondo = new Path();
        fondo.getStyleClass().add("cristal-fondo");
        fondo.setManaged(false);
        fondo.setMouseTransparent(true);
        Line bordeSuperior = new Line();
        Line bordeInferior = new Line();
        for (Line borde : new Line[]{bordeSuperior, bordeInferior}) {
            borde.getStyleClass().add("cristal-borde-vivo");
            borde.setManaged(false);
            borde.setMouseTransparent(true);
        }
        tarjeta.getChildren().addAll(0, List.of(fondo, bordeSuperior, bordeInferior));

        InvalidationListener actualizar = observable -> {
            double w = tarjeta.getWidth();
            double h = tarjeta.getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            fondo.getElements().setAll(
                    new MoveTo(corte, 0),
                    new LineTo(w - RADIO, 0),
                    new QuadCurveTo(w, 0, w, RADIO),
                    new LineTo(w, h - corte),
                    new LineTo(w - corte, h),
                    new LineTo(RADIO, h),
                    new QuadCurveTo(0, h, 0, h - RADIO),
                    new LineTo(0, corte),
                    new ClosePath());
            bordeSuperior.setStartX(0);
            bordeSuperior.setStartY(corte);
            bordeSuperior.setEndX(corte);
            bordeSuperior.setEndY(0);
            bordeInferior.setStartX(w - corte);
            bordeInferior.setStartY(h);
            bordeInferior.setEndX(w);
            bordeInferior.setEndY(h - corte);
        };
        tarjeta.widthProperty().addListener(actualizar);
        tarjeta.heightProperty().addListener(actualizar);
        actualizar.invalidated(null);
    }
}
