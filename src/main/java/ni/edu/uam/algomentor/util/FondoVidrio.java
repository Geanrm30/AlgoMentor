package ni.edu.uam.algomentor.util;

import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.ColorAdjust;
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
    private static double luminosidad = 0.4;
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

    /** Luminosidad media de 0 (negro) a 1 (blanco) de una muestra de píxeles ARGB. */
    public static double luminanciaMedia(int[] argb) {
        if (argb == null || argb.length == 0) {
            return 0;
        }
        double suma = 0;
        for (int pixel : argb) {
            suma += (0.2126 * ((pixel >> 16) & 0xFF) + 0.7152 * ((pixel >> 8) & 0xFF) + 0.0722 * (pixel & 0xFF)) / 255.0;
        }
        return suma / argb.length;
    }

    /**
     * Opacidad del tinte que se pone encima de la captura para que el texto se lea. Un fondo oscuro necesita poco tinte
     * (si no, queda negro y no se nota el vidrio); uno claro necesita más. {@code base} es el tinte con luminosidad media.
     */
    public static double opacidadTinte(double luminosidad, boolean temaClaro, double base) {
        double l = Math.max(0, Math.min(1, luminosidad));
        double opacidad = temaClaro ? base + 0.20 * (0.5 - l) : base * (0.35 + 1.3 * l);
        return Math.max(temaClaro ? 0.32 : 0.10, Math.min(temaClaro ? 0.70 : 0.62, opacidad));
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
            int[] util = java.util.Arrays.copyOf(muestra, k);
            if (pareceVacia(util)) {
                return null;
            }
            luminosidad = luminanciaMedia(util);
            return imagen;
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
        boolean[] atento = {false};
        Runnable programar = () -> {
            // Al abrir, la ventana cambia de posición y tamaño varias veces: eso no cuenta.
            if (!atento[0]) {
                return;
            }
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
        ventana.showingProperty().addListener((obs, antes, ahora) -> {
            if (ahora) {
                PauseTransition arranque = new PauseTransition(Duration.millis(3000));
                arranque.setOnFinished(evento -> atento[0] = true);
                arranque.play();
            }
        });
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
        double fondo = opacidadTinte(luminosidad, claro, claro ? 0.50 : 0.52);
        double panel = opacidadTinte(luminosidad, claro, claro ? 0.46 : 0.36);
        // Si la captura es muy oscura, se le sube un poco el brillo y el contraste para que se distinga lo de atrás.
        boolean oscura = !claro && luminosidad < 0.25;
        NITIDA.set(procesar(tomada, 5, claro ? Color.rgb(255, 255, 255, fondo) : Color.rgb(8, 10, 16, fondo), null, oscura));
        BORROSA.set(procesar(tomada, 13, claro ? Color.rgb(255, 255, 255, panel) : Color.rgb(8, 10, 16, panel),
                claro ? Color.rgb(255, 255, 255, 0.10) : Color.rgb(255, 255, 255, 0.07), oscura));
    }

    /**
     * Reduce la captura, la desenfoca y la tiñe. Se dibuja un poco más grande que el área visible para que el borde
     * del desenfoque (que se desvanece hacia lo transparente) quede fuera.
     */
    private static Image procesar(Image origen, double radio, Color tinte, Color brillo, boolean realzar) {
        ImageView vista = new ImageView(origen);
        vista.setPreserveRatio(false);
        vista.setSmooth(true);
        vista.setFitWidth(ANCHO_BASE + 60);
        vista.setFitHeight(ALTO_BASE + 34);
        vista.setLayoutX(-30);
        vista.setLayoutY(-17);
        GaussianBlur desenfoque = new GaussianBlur(radio);
        if (realzar) {
            ColorAdjust ajuste = new ColorAdjust(0, 0.25, 0.28, 0.22);   // tono, saturación, brillo, contraste
            ajuste.setInput(desenfoque);
            vista.setEffect(ajuste);
        } else {
            vista.setEffect(desenfoque);
        }
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
