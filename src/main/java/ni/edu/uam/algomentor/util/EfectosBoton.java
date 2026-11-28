package ni.edu.uam.algomentor.util;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Region;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.Locale;

/**
 * Microanimaciones de botón. Cada botón recibe un ícono según su texto (eliminar, actualizar, guardar...) y una
 * animación propia. Las animaciones de acciones que pueden fallar o cancelarse (guardar, eliminar, verificar)
 * no se disparan al hacer clic: el controlador las lanza con {@link #exito(Button)} cuando la acción termina bien.
 */
public final class EfectosBoton {

    public enum Anim { PAPELERA, GIRO, GIRO_INVERSO, CONFIRMAR, SUBIR, BAJAR, PULSO }

    public enum Tipo {
        ELIMINAR("papelera", Anim.PAPELERA, false),
        ACTUALIZAR("actualizar", Anim.GIRO, true),
        GUARDAR("disco", Anim.CONFIRMAR, false),
        EXPORTAR("subir", Anim.SUBIR, true),
        IMPORTAR("bajar", Anim.BAJAR, true),
        GENERAR("chispas", Anim.GIRO, true),
        LIMPIAR("deshacer", Anim.GIRO_INVERSO, true),
        SIMULAR("play", Anim.PULSO, true),
        VERIFICAR("check", Anim.CONFIRMAR, false),
        VER("ojo", Anim.PULSO, true),
        BUSCAR("buscar", Anim.PULSO, true),
        VOLVER("volver", Anim.PULSO, true),
        COMPARAR("comparar", Anim.PULSO, true);

        private final String icono;
        private final Anim animacion;
        private final boolean alHacerClic;

        Tipo(String icono, Anim animacion, boolean alHacerClic) {
            this.icono = icono;
            this.animacion = animacion;
            this.alHacerClic = alHacerClic;
        }

        public String icono() { return icono; }
        public Anim animacion() { return animacion; }
        public boolean alHacerClic() { return alHacerClic; }
    }

    private static final String CLAVE_TIPO = "algomentor.efectoTipo";
    private static final String CLAVE_ACTIVO = "algomentor.efectoActivo";

    private EfectosBoton() { }

    /** Tipo de efecto que corresponde al texto de un botón, o null si no hay ninguno adecuado. */
    public static Tipo tipoPara(String texto) {
        if (texto == null) {
            return null;
        }
        String t = texto.toLowerCase(Locale.ROOT).strip();
        if (t.isEmpty()) return null;
        if (t.contains("eliminar") || t.contains("borrar") || t.contains("quitar")) return Tipo.ELIMINAR;
        if (t.contains("actualizar") || t.contains("recargar")) return Tipo.ACTUALIZAR;
        if (t.contains("guardar")) return Tipo.GUARDAR;
        if (t.contains("exportar")) return Tipo.EXPORTAR;
        if (t.contains("importar")) return Tipo.IMPORTAR;
        if (t.startsWith("generar")) return Tipo.GENERAR;
        if (t.contains("limpiar") || t.contains("restablecer")) return Tipo.LIMPIAR;
        if (t.contains("vista previa") || t.startsWith("ver ")) return Tipo.VER;
        if (t.contains("buscar")) return Tipo.BUSCAR;
        if (t.startsWith("volver")) return Tipo.VOLVER;
        if (t.startsWith("comparar")) return Tipo.COMPARAR;
        if (t.equals("simular") || t.startsWith("resolver")) return Tipo.SIMULAR;
        if (t.equals("verificar") || t.startsWith("comprobar") || t.equals("responder")) return Tipo.VERIFICAR;
        return null;
    }

    /** Pone ícono y animación a los botones de una vista que todavía no tengan gráfico. */
    public static void decorar(Node raiz) {
        if (raiz == null) {
            return;
        }
        for (Node nodo : raiz.lookupAll(".button")) {
            if (nodo instanceof Button boton && boton.getGraphic() == null
                    && !boton.getStyleClass().contains("item-nav")
                    && !boton.getProperties().containsKey(CLAVE_TIPO)) {
                Tipo tipo = tipoPara(boton.getText());
                if (tipo != null) {
                    instalar(boton, tipo);
                }
            }
        }
    }

    public static void instalar(Button boton, Tipo tipo) {
        boton.setGraphic(tipo == Tipo.ELIMINAR ? Iconos.papelera() : Iconos.crearBoton(tipo.icono()));
        boton.setGraphicTextGap(8);
        boton.setContentDisplay(ContentDisplay.LEFT);
        boton.getProperties().put(CLAVE_TIPO, tipo);
        if (tipo.alHacerClic()) {
            boton.addEventHandler(ActionEvent.ACTION, evento -> animar(boton, tipo));
        }
    }

    /** La acción del botón terminó bien: reproduce su animación (papelera, check, giro...). */
    public static void exito(Button boton) {
        if (boton.getProperties().get(CLAVE_TIPO) instanceof Tipo tipo) {
            animar(boton, tipo);
        }
    }

    /** La acción falló: el botón se pone rojo con una cruz y se sacude. */
    public static void fallo(Button boton) {
        if (ocupado(boton)) {
            return;
        }
        marcarActivo(boton, true);
        Node original = boton.getGraphic();
        boton.setGraphic(Iconos.crearBoton("cerrar"));
        boton.getStyleClass().add("boton-error");
        Animaciones.sacudir(boton);
        PauseTransition espera = new PauseTransition(Duration.millis(900));
        espera.setOnFinished(evento -> {
            boton.getStyleClass().remove("boton-error");
            boton.setGraphic(original);
            marcarActivo(boton, false);
        });
        espera.play();
    }

    /** Resultado de una verificación: éxito (check verde) o fallo (cruz roja). */
    public static void resultado(Button boton, boolean correcto) {
        if (correcto) {
            exito(boton);
        } else {
            fallo(boton);
        }
    }

    private static boolean ocupado(Button boton) {
        return Boolean.TRUE.equals(boton.getProperties().get(CLAVE_ACTIVO));
    }

    private static void marcarActivo(Button boton, boolean activo) {
        if (activo) {
            boton.getProperties().put(CLAVE_ACTIVO, Boolean.TRUE);
        } else {
            boton.getProperties().remove(CLAVE_ACTIVO);
        }
    }

    private static void animar(Button boton, Tipo tipo) {
        if (ocupado(boton) || boton.getGraphic() == null) {
            return;
        }
        Node icono = boton.getGraphic();
        switch (tipo.animacion()) {
            case PAPELERA -> papelera(boton, icono);
            case GIRO -> girar(boton, icono, 360);
            case GIRO_INVERSO -> girar(boton, icono, -360);
            case SUBIR -> deslizar(boton, icono, -10);
            case BAJAR -> deslizar(boton, icono, 10);
            case PULSO -> pulso(boton, icono);
            case CONFIRMAR -> confirmar(boton, tipo);
        }
    }

    private static void terminar(Button boton, Animation animacion, Runnable alFinal) {
        marcarActivo(boton, true);
        animacion.setOnFinished(evento -> {
            if (alFinal != null) {
                alFinal.run();
            }
            marcarActivo(boton, false);
        });
        animacion.play();
    }

    private static void girar(Button boton, Node icono, double grados) {
        RotateTransition giro = new RotateTransition(Duration.millis(650), icono);
        giro.setByAngle(grados);
        giro.setInterpolator(Interpolator.EASE_BOTH);
        terminar(boton, giro, () -> icono.setRotate(0));
    }

    private static void pulso(Button boton, Node icono) {
        ScaleTransition crecer = new ScaleTransition(Duration.millis(150), icono);
        crecer.setToX(1.4);
        crecer.setToY(1.4);
        crecer.setCycleCount(2);
        crecer.setAutoReverse(true);
        TranslateTransition empuje = new TranslateTransition(Duration.millis(150), icono);
        empuje.setToX(3);
        empuje.setCycleCount(2);
        empuje.setAutoReverse(true);
        terminar(boton, new ParallelTransition(crecer, empuje), () -> {
            icono.setScaleX(1);
            icono.setScaleY(1);
            icono.setTranslateX(0);
        });
    }

    /** El ícono sale del botón por un lado y vuelve a entrar por el opuesto. */
    private static void deslizar(Button boton, Node icono, double distancia) {
        TranslateTransition salir = new TranslateTransition(Duration.millis(160), icono);
        salir.setToY(distancia);
        FadeTransition desvanecer = new FadeTransition(Duration.millis(160), icono);
        desvanecer.setToValue(0);
        ParallelTransition ida = new ParallelTransition(salir, desvanecer);
        PauseTransition salto = new PauseTransition(Duration.millis(1));
        salto.setOnFinished(evento -> icono.setTranslateY(-distancia));
        TranslateTransition entrar = new TranslateTransition(Duration.millis(220), icono);
        entrar.setToY(0);
        entrar.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition aparecer = new FadeTransition(Duration.millis(220), icono);
        aparecer.setToValue(1);
        terminar(boton, new SequentialTransition(ida, salto, new ParallelTransition(entrar, aparecer)), () -> {
            icono.setTranslateY(0);
            icono.setOpacity(1);
        });
    }

    /** Guardar y verificar: el ícono se cambia por un check con rebote y el botón se pone verde un instante. */
    private static void confirmar(Button boton, Tipo tipo) {
        marcarActivo(boton, true);
        Node original = boton.getGraphic();
        String textoOriginal = boton.getText();
        Region check = Iconos.crearBoton("check");
        check.setScaleX(0.2);
        check.setScaleY(0.2);
        boton.setGraphic(check);
        if (tipo == Tipo.GUARDAR) {
            boton.setText("Guardado");
        }
        boton.getStyleClass().add("boton-exito");
        ScaleTransition rebote = new ScaleTransition(Duration.millis(360), check);
        rebote.setToX(1.25);
        rebote.setToY(1.25);
        rebote.setInterpolator(Interpolator.EASE_OUT);
        ScaleTransition asentar = new ScaleTransition(Duration.millis(140), check);
        asentar.setToX(1);
        asentar.setToY(1);
        PauseTransition mantener = new PauseTransition(Duration.millis(700));
        SequentialTransition total = new SequentialTransition(rebote, asentar, mantener);
        total.setOnFinished(evento -> {
            boton.getStyleClass().remove("boton-exito");
            boton.setGraphic(original);
            boton.setText(textoOriginal);
            marcarActivo(boton, false);
        });
        total.play();
    }

    /**
     * El botón se encoge hasta ser un círculo, la tapa de la papelera se abre dos veces y el botón vuelve a
     * su tamaño. El ancho se fija durante la animación para que la interpolación sea suave.
     */
    private static void papelera(Button boton, Node icono) {
        double ancho = boton.getWidth();
        double alto = boton.getHeight();
        if (ancho <= alto || !(icono.getProperties().get(Iconos.CLAVE_TAPA) instanceof Node tapa)) {
            return;
        }
        marcarActivo(boton, true);
        String estiloOriginal = boton.getStyle();
        Rotate bisagra = new Rotate(0, 5, 6);
        tapa.getTransforms().add(bisagra);
        fijarAncho(boton, ancho);
        boton.setStyle(estiloOriginal + "-fx-background-radius: 999;");

        Timeline encoger = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(boton.prefWidthProperty(), ancho, Interpolator.EASE_BOTH),
                        new KeyValue(boton.minWidthProperty(), ancho, Interpolator.EASE_BOTH),
                        new KeyValue(boton.maxWidthProperty(), ancho, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(260), evento -> boton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY),
                        new KeyValue(boton.prefWidthProperty(), alto, Interpolator.EASE_BOTH),
                        new KeyValue(boton.minWidthProperty(), alto, Interpolator.EASE_BOTH),
                        new KeyValue(boton.maxWidthProperty(), alto, Interpolator.EASE_BOTH)));
        Timeline abrir = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(bisagra.angleProperty(), 0)),
                new KeyFrame(Duration.millis(120), new KeyValue(bisagra.angleProperty(), -32, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(240), new KeyValue(bisagra.angleProperty(), 0, Interpolator.EASE_IN)),
                new KeyFrame(Duration.millis(360), new KeyValue(bisagra.angleProperty(), -32, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(480), new KeyValue(bisagra.angleProperty(), 0, Interpolator.EASE_IN)));
        ScaleTransition latido = new ScaleTransition(Duration.millis(240), icono);
        latido.setToX(1.3);
        latido.setToY(1.3);
        latido.setCycleCount(2);
        latido.setAutoReverse(true);
        Timeline expandir = new Timeline(
                new KeyFrame(Duration.ZERO, evento -> boton.setContentDisplay(ContentDisplay.LEFT),
                        new KeyValue(boton.prefWidthProperty(), alto, Interpolator.EASE_BOTH),
                        new KeyValue(boton.minWidthProperty(), alto, Interpolator.EASE_BOTH),
                        new KeyValue(boton.maxWidthProperty(), alto, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(260),
                        new KeyValue(boton.prefWidthProperty(), ancho, Interpolator.EASE_BOTH),
                        new KeyValue(boton.minWidthProperty(), ancho, Interpolator.EASE_BOTH),
                        new KeyValue(boton.maxWidthProperty(), ancho, Interpolator.EASE_BOTH)));
        SequentialTransition total = new SequentialTransition(encoger, new ParallelTransition(abrir, latido), expandir);
        total.setOnFinished(evento -> {
            tapa.getTransforms().remove(bisagra);
            boton.setStyle(estiloOriginal);
            boton.setMinWidth(Region.USE_COMPUTED_SIZE);
            boton.setPrefWidth(Region.USE_COMPUTED_SIZE);
            boton.setMaxWidth(Region.USE_COMPUTED_SIZE);
            boton.setContentDisplay(ContentDisplay.LEFT);
            marcarActivo(boton, false);
        });
        total.play();
    }

    private static void fijarAncho(Button boton, double ancho) {
        boton.setMinWidth(ancho);
        boton.setPrefWidth(ancho);
        boton.setMaxWidth(ancho);
    }
}
