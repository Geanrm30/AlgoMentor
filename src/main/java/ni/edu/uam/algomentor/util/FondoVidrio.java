package ni.edu.uam.algomentor.util;

import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fondo de vidrio: captura lo que hay detrás de la ventana (el escritorio, IntelliJ...), lo desenfoca y lo usa como
 * fondo de la aplicación. Los paneles ({@link VidrioEsmerilado}) muestran una copia todavía más desenfocada. La captura
 * se toma antes de mostrar la ventana y se repite al moverla o redimensionarla, ocultándola un instante.
 */
public final class FondoVidrio {

    private static final Logger LOG = Logger.getLogger(FondoVidrio.class.getName());
    public static final String CLASE = "fondo-malla";
    private static final int ANCHO_BASE = 480;
    private static final int ALTO_BASE = 270;

    private static final ObjectProperty<Image> NITIDA = new SimpleObjectProperty<>();
    private static final ObjectProperty<Image> BORROSA = new SimpleObjectProperty<>();
    private static final BooleanProperty ACTIVO = new SimpleBooleanProperty(false);
    private static Image captura;
    private static boolean claro;
    private static boolean permitido;
    private static PauseTransition espera;
    private static long ultimaCaptura;

    private FondoVidrio() { }

    public static ObjectProperty<Image> borrosaProperty() { return BORROSA; }
    public static BooleanProperty activoProperty() { return ACTIVO; }
    public static boolean activo() { return ACTIVO.get(); }

    // ---------- Cálculos puros ----------

    /** Una captura negra o sin variación no sirve (por ejemplo, sin permiso de grabación de pantalla). */
    public static boolean pareceVacia(int[] argb) {
        if (argb == null || argb.length == 0) {
            return true;
        }
        long suma = 0;
        int minimo = 255;
        int maximo = 0;
        for (int pixel : argb) {
            int luz = (((pixel >> 16) & 0xFF) + ((pixel >> 8) & 0xFF) + (pixel & 0xFF)) / 3;
            suma += luz;
            minimo = Math.min(minimo, luz);
            maximo = Math.max(maximo, luz);
        }
        return suma / argb.length < 2 || maximo - minimo < 3;
    }

    // ---------- Captura ----------

    /** Toma lo que hay en {@code area} de la pantalla. Debe llamarse en el hilo de JavaFX con la ventana oculta. */
    private static Image tomar(Rectangle2D area) {
        try {
            WritableImage imagen = new Robot().getScreenCapture(null, area);
            PixelReader lector = imagen.getPixelReader();
            int ancho = (int) imagen.getWidth();
            int alto = (int) imagen.getHeight();
            int paso = Math.max(1, Math.min(ancho, alto) / 40);
            int[] muestra = new int[((ancho / paso) + 1) * ((alto / paso) + 1)];
            int k = 0;
            for (int y = 0; y < alto; y += paso) {
                for (int x = 0; x < ancho; x += paso) {
                    muestra[k++] = lector.getArgb(x, y);
                }
            }
            return pareceVacia(java.util.Arrays.copyOf(muestra, k)) ? null : imagen;
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo capturar lo que hay detrás de la ventana", e);
            return null;
        }
    }

    /** Primera captura, antes de mostrar la ventana. Devuelve false si no fue posible (se usa la malla de color). */
    public static boolean iniciar(Rectangle2D area, boolean temaClaro) {
        claro = temaClaro;
        Image tomada = tomar(area);
        if (tomada == null) {
            permitido = false;
            ACTIVO.set(false);
            return false;
        }
        aplicar(tomada);
        permitido = true;
        ACTIVO.set(true);
        return true;
    }

    /** Oculta la ventana un instante, vuelve a capturar y la muestra de nuevo. */
    public static void recapturar(Stage ventana, Runnable alTerminar) {
        if (!permitido || ventana == null || !ventana.isShowing() || ventana.isIconified()) {
            return;
        }
        Rectangle2D area = new Rectangle2D(ventana.getX(), ventana.getY(), ventana.getWidth(), ventana.getHeight());
        ventana.setOpacity(0);
        PauseTransition pausa = new PauseTransition(Duration.millis(220));
        pausa.setOnFinished(evento -> {
            Image nueva = tomar(area);
            ventana.setOpacity(1);
            if (nueva != null) {
                aplicar(nueva);
                ACTIVO.set(true);
            }
            if (alTerminar != null) {
                alTerminar.run();
            }
        });
        pausa.play();
    }

    /** Vuelve a capturar cuando la ventana deja de moverse o cambiar de tamaño. */
    public static void vigilar(Stage ventana) {
        Runnable programar = () -> {
            if (!permitido) {
                return;
            }
            if (espera != null) {
                espera.stop();
            }
            espera = new PauseTransition(Duration.millis(700));
            espera.setOnFinished(evento -> recapturar(ventana, null));
            espera.play();
        };
        ventana.xProperty().addListener((obs, a, b) -> programar.run());
        ventana.yProperty().addListener((obs, a, b) -> programar.run());
        ventana.widthProperty().addListener((obs, a, b) -> programar.run());
        ventana.heightProperty().addListener((obs, a, b) -> programar.run());
        ventana.focusedProperty().addListener((obs, antes, ahora) -> {
            // Al volver a la ventana tras un rato, lo que había detrás pudo haber cambiado.
            if (ahora && System.currentTimeMillis() - ultimaCaptura > 60_000) {
                programar.run();
            }
        });
    }

    /**
     * Enciende o apaga el fondo de vidrio. Al encenderlo sin captura previa, la toma enseguida (con un parpadeo de la
     * ventana) y avisa si no fue posible.
     */
    public static void activar(boolean encendido, Stage ventana, Consumer<Boolean> alTerminar) {
        permitido = encendido;
        if (!encendido) {
            ACTIVO.set(false);
            if (alTerminar != null) {
                alTerminar.accept(false);
            }
            return;
        }
        if (captura != null) {
            ACTIVO.set(true);
            if (alTerminar != null) {
                alTerminar.accept(true);
            }
            return;
        }
        recapturar(ventana, () -> {
            boolean logrado = captura != null;
            permitido = logrado;
            ACTIVO.set(logrado);
            if (alTerminar != null) {
                alTerminar.accept(logrado);
            }
        });
    }

    public static boolean hayCaptura() {
        return captura != null;
    }

    /** El tema cambia el tinte con el que se oscurece o aclara el fondo. */
    public static void cambiarTema(boolean temaClaro) {
        claro = temaClaro;
        if (captura != null) {
            aplicar(captura);
        }
    }

    private static void aplicar(Image tomada) {
        captura = tomada;
        ultimaCaptura = System.currentTimeMillis();
        NITIDA.set(procesar(tomada, 5, claro ? Color.rgb(255, 255, 255, 0.50) : Color.rgb(8, 10, 16, 0.50), null));
        BORROSA.set(procesar(tomada, 13, claro ? Color.rgb(255, 255, 255, 0.46) : Color.rgb(8, 10, 16, 0.34),
                claro ? Color.rgb(255, 255, 255, 0.10) : Color.rgb(255, 255, 255, 0.07)));
    }

    /**
     * Reduce la captura, la desenfoca y la tiñe. Se dibuja un poco más grande que el área visible para que el borde
     * del desenfoque (que se desvanece hacia lo transparente) quede fuera.
     */
    private static Image procesar(Image origen, double radio, Color tinte, Color brillo) {
        ImageView vista = new ImageView(origen);
        vista.setPreserveRatio(false);
        vista.setSmooth(true);
        vista.setFitWidth(ANCHO_BASE + 60);
        vista.setFitHeight(ALTO_BASE + 34);
        vista.setLayoutX(-30);
        vista.setLayoutY(-17);
        vista.setEffect(new GaussianBlur(radio));
        Group grupo = new Group(vista);
        Pane lienzo = new Pane(grupo);
        lienzo.setPrefSize(ANCHO_BASE, ALTO_BASE);
        lienzo.setMinSize(ANCHO_BASE, ALTO_BASE);
        lienzo.setMaxSize(ANCHO_BASE, ALTO_BASE);
        lienzo.setClip(new Rectangle(ANCHO_BASE, ALTO_BASE));
        Rectangle capaTinte = new Rectangle(ANCHO_BASE, ALTO_BASE, tinte);
        lienzo.getChildren().add(capaTinte);
        if (brillo != null) {
            lienzo.getChildren().add(new Rectangle(ANCHO_BASE, ALTO_BASE, brillo));
        }
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.BLACK);
        return lienzo.snapshot(parametros, new WritableImage(ANCHO_BASE, ALTO_BASE));
    }

    // ---------- Fondo de la ventana ----------

    /** Pone la captura difuminada detrás de todo el contenido, en lugar de la malla de color. */
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
