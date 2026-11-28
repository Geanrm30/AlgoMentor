package ni.edu.uam.algomentor.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Vidrio esmerilado sobre el fondo morado de la aplicación. El fondo (la malla de color con cintas tenues) se dibuja una
 * sola vez como imagen; esa imagen es el fondo de la ventana y, además, cada panel ({@link VidrioEsmerilado}) muestra una
 * copia suya muy desenfocada colocada justo donde estaría el fondo, de modo que se ve como vidrio esmerilado.
 */
public final class FondoVidrio {

    public static final String CLASE = "fondo-malla";
    private static final double ANCHO_FONDO = 1600;
    private static final double ALTO_FONDO = 900;
    private static final int ANCHO_PANEL = 480;
    private static final int ALTO_PANEL = 270;

    private static final ObjectProperty<Image> NITIDA = new SimpleObjectProperty<>();
    private static final ObjectProperty<Image> BORROSA = new SimpleObjectProperty<>();
    private static final BooleanProperty ACTIVO = new SimpleBooleanProperty(false);
    private static boolean preparado;

    private FondoVidrio() { }

    public static ObjectProperty<Image> borrosaProperty() { return BORROSA; }
    public static BooleanProperty activoProperty() { return ACTIVO; }
    public static boolean activo() { return ACTIVO.get(); }

    /** Indica si ya hay imágenes preparadas (aunque el efecto esté apagado, para poder encenderlo al instante). */
    public static boolean hayCaptura() { return preparado; }

    /** Dibuja el fondo y su copia desenfocada para el tema dado. Debe llamarse en el hilo de JavaFX. */
    public static void preparar(boolean temaClaro) {
        Pane fondo = Fondo.mallaConCintas(temaClaro, false, 0.45);
        fondo.setPrefSize(ANCHO_FONDO, ALTO_FONDO);
        fondo.resize(ANCHO_FONDO, ALTO_FONDO);
        // Las cintas se salen del área; sin recorte, la imagen saldría desplazada.
        fondo.setClip(new Rectangle(ANCHO_FONDO, ALTO_FONDO));
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(temaClaro ? Color.web("#eef0f8") : Color.web("#080c1a"));
        Image nitida = fondo.snapshot(parametros, new WritableImage((int) ANCHO_FONDO, (int) ALTO_FONDO));
        NITIDA.set(nitida);
        BORROSA.set(difuminar(nitida, temaClaro));
        preparado = true;
    }

    public static void activar(boolean encendido) {
        ACTIVO.set(encendido && preparado);
    }

    /**
     * Copia pequeña y muy difuminada del fondo, con un velo claro encima para que el panel se lea como vidrio. Se dibuja
     * un poco más grande que el área visible para que el borde del desenfoque (que se desvanece) quede fuera.
     */
    private static Image difuminar(Image origen, boolean temaClaro) {
        ImageView vista = new ImageView(origen);
        vista.setPreserveRatio(false);
        vista.setSmooth(true);
        vista.setFitWidth(ANCHO_PANEL + 60);
        vista.setFitHeight(ALTO_PANEL + 34);
        vista.setLayoutX(-30);
        vista.setLayoutY(-17);
        vista.setEffect(new GaussianBlur(11));
        Pane lienzo = new Pane(new Group(vista));
        lienzo.setPrefSize(ANCHO_PANEL, ALTO_PANEL);
        lienzo.setMinSize(ANCHO_PANEL, ALTO_PANEL);
        lienzo.setMaxSize(ANCHO_PANEL, ALTO_PANEL);
        lienzo.setClip(new Rectangle(ANCHO_PANEL, ALTO_PANEL));
        Color velo = temaClaro ? Color.rgb(255, 255, 255, 0.34) : Color.rgb(255, 255, 255, 0.075);
        lienzo.getChildren().add(new Rectangle(ANCHO_PANEL, ALTO_PANEL, velo));
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(temaClaro ? Color.web("#eef0f8") : Color.web("#080c1a"));
        return lienzo.snapshot(parametros, new WritableImage(ANCHO_PANEL, ALTO_PANEL));
    }

    /** Pone la imagen del fondo detrás de todo el contenido, en lugar de la malla dibujada en vivo. */
    public static void instalar(StackPane contenedor) {
        contenedor.getChildren().removeIf(nodo -> nodo.getStyleClass().contains(CLASE));
        ImageView fondo = new ImageView();
        fondo.getStyleClass().add(CLASE);
        fondo.setPreserveRatio(false);
        fondo.setSmooth(true);
        fondo.setMouseTransparent(true);
        fondo.imageProperty().bind(NITIDA);
        fondo.setManaged(false);
        fondo.fitWidthProperty().bind(contenedor.widthProperty());
        fondo.fitHeightProperty().bind(contenedor.heightProperty());
        contenedor.getChildren().add(0, fondo);
    }
}
