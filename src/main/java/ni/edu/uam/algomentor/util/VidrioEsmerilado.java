package ni.edu.uam.algomentor.util;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.function.BiFunction;

/**
 * Convierte un panel en vidrio esmerilado: dentro lleva una copia desenfocada del fondo colocada exactamente donde
 * estaría el fondo real, de modo que a través del panel se ve lo de atrás, difuminado. Sigue al panel al desplazarse,
 * y solo se ve mientras el fondo de vidrio está activo.
 */
public final class VidrioEsmerilado {

    private static final String MARCA = "algomentor.vidrioEsmerilado";

    private VidrioEsmerilado() { }

    /** Aplica el vidrio a las tarjetas, a la barra lateral y a las tarjetas de resumen de una vista. */
    public static void decorar(Parent raiz) {
        if (raiz == null) {
            return;
        }
        for (Node nodo : raiz.lookupAll(".card, .lateral, .summary-card")) {
            if (nodo instanceof Pane panel && !panel.getProperties().containsKey(MARCA)
                    && !nodo.getStyleClass().contains("tutorial-card")) {
                instalar(panel, nodo.getStyleClass().contains("lateral") ? 26 : 20, null);
            }
        }
    }

    /**
     * @param forma si no es null, construye el recorte con el ancho y el alto del panel (para formas como la tarjeta
     *              del inicio de sesión, con esquinas cortadas); si es null, se recorta con esquinas redondeadas.
     */
    public static void instalar(Pane panel, double radio, BiFunction<Double, Double, Shape> forma) {
        if (panel.getProperties().containsKey(MARCA)) {
            return;
        }
        panel.getProperties().put(MARCA, Boolean.TRUE);
        ImageView copia = new ImageView();
        copia.setPreserveRatio(false);
        copia.setSmooth(true);
        copia.setManaged(false);
        copia.setMouseTransparent(true);
        copia.imageProperty().bind(FondoVidrio.borrosaProperty());
        copia.visibleProperty().bind(FondoVidrio.activoProperty());
        Rectangle redondeado = new Rectangle();
        redondeado.setArcWidth(2 * radio - 2);
        redondeado.setArcHeight(2 * radio - 2);
        if (forma == null) {
            copia.setClip(redondeado);
        }
        Runnable colocar = () -> {
            Scene escena = panel.getScene();
            if (escena == null) {
                return;
            }
            Point2D origen = panel.sceneToLocal(0, 0);
            if (origen == null) {
                return;
            }
            copia.setFitWidth(escena.getWidth());
            copia.setFitHeight(escena.getHeight());
            copia.setLayoutX(origen.getX());
            copia.setLayoutY(origen.getY());
            if (forma == null) {
                redondeado.setX(-origen.getX() + 1);
                redondeado.setY(-origen.getY() + 1);
                redondeado.setWidth(Math.max(0, panel.getWidth() - 2));
                redondeado.setHeight(Math.max(0, panel.getHeight() - 2));
            } else {
                Shape recorte = forma.apply(panel.getWidth(), panel.getHeight());
                recorte.setLayoutX(-origen.getX());
                recorte.setLayoutY(-origen.getY());
                copia.setClip(recorte);
            }
        };
        panel.localToSceneTransformProperty().addListener(observable -> colocar.run());
        panel.widthProperty().addListener(observable -> colocar.run());
        panel.heightProperty().addListener(observable -> colocar.run());
        panel.sceneProperty().addListener((obs, antes, escena) -> {
            if (escena != null) {
                escena.widthProperty().addListener(observable -> colocar.run());
                escena.heightProperty().addListener(observable -> colocar.run());
                colocar.run();
            }
        });
        panel.getChildren().add(0, copia);
        if (panel.getScene() != null) {
            panel.getScene().widthProperty().addListener(observable -> colocar.run());
            panel.getScene().heightProperty().addListener(observable -> colocar.run());
        }
        colocar.run();
    }
}
