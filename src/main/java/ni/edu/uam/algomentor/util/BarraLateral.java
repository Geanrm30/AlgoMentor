package ni.edu.uam.algomentor.util;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Barra de navegación lateral con un indicador que se desliza hasta la sección elegida. */
public final class BarraLateral extends VBox {

    public record Elemento(String clave, String texto, String icono, String atajo, Runnable accion) { }

    public record Grupo(String titulo, List<Elemento> elementos) { }

    public static final double ANCHO_ABIERTA = 256;
    public static final double ANCHO_CONTRAIDA = 76;
    private static final double ALTO_ELEMENTO = 42;

    private final Map<String, Button> botones = new LinkedHashMap<>();
    private final List<Label> titulos = new java.util.ArrayList<>();
    private final Region indicador = new Region();
    private final VBox lista = new VBox(4);
    private final Label marca = new Label("AlgoMentor");
    private final Consumer<Boolean> alContraer;
    private Timeline movimiento;
    private Timeline ancho;
    private String seleccion;
    private boolean contraida;

    public BarraLateral(List<Grupo> grupos, boolean contraidaInicial, Consumer<Boolean> alContraer) {
        this.alContraer = alContraer == null ? c -> { } : alContraer;
        getStyleClass().add("lateral");
        setSpacing(8);
        setPadding(new Insets(14, 12, 14, 12));

        marca.getStyleClass().add("brand");
        Button contraer = new Button();
        contraer.setGraphic(Iconos.crear("menu"));
        contraer.getStyleClass().add("item-nav");
        contraer.setTooltip(new Tooltip("Contraer o expandir el menú"));
        contraer.setOnAction(evento -> alternarContraida());
        contraer.setMinWidth(44);
        contraer.setPrefHeight(ALTO_ELEMENTO);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox cabecera = new HBox(marca, espacio, contraer);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        marca.setPadding(new Insets(0, 0, 0, 10));
        getChildren().add(cabecera);

        for (Grupo grupo : grupos) {
            Label titulo = new Label(grupo.titulo().toUpperCase());
            titulo.getStyleClass().add("nav-grupo");
            titulos.add(titulo);
            lista.getChildren().add(titulo);
            for (Elemento elemento : grupo.elementos()) {
                Button boton = new Button(elemento.texto(), Iconos.crear(elemento.icono()));
                boton.getStyleClass().add("item-nav");
                boton.setGraphicTextGap(14);
                boton.setMaxWidth(Double.MAX_VALUE);
                boton.setMinHeight(ALTO_ELEMENTO);
                boton.setPrefHeight(ALTO_ELEMENTO);
                boton.setMaxHeight(ALTO_ELEMENTO);
                boton.setTooltip(new Tooltip(elemento.texto() + (elemento.atajo() == null ? "" : "  (" + elemento.atajo() + ")")));
                boton.setOnAction(evento -> {
                    seleccionar(elemento.clave());
                    elemento.accion().run();
                });
                botones.put(elemento.clave(), boton);
                lista.getChildren().add(boton);
            }
        }
        indicador.getStyleClass().add("indicador-nav");
        indicador.setMouseTransparent(true);
        indicador.setVisible(false);
        Pane capaIndicador = new Pane(indicador);
        capaIndicador.setMouseTransparent(true);
        capaIndicador.setPickOnBounds(false);
        StackPane contenido = new StackPane(capaIndicador, lista);
        ScrollPane desplazamiento = new ScrollPane(contenido);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.getStyleClass().add("nav-scroll");
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);
        getChildren().add(desplazamiento);

        // Si cambia el tamaño o la distribución, el indicador se recoloca sin animación.
        lista.widthProperty().addListener((obs, antes, ahora) -> colocar(false));
        lista.heightProperty().addListener((obs, antes, ahora) -> colocar(false));

        aplicarContraccion(contraidaInicial, false);
    }

    public boolean contraida() {
        return contraida;
    }

    public String seleccion() {
        return seleccion;
    }

    public boolean contiene(String clave) {
        return botones.containsKey(clave);
    }

    public Button boton(String clave) {
        return botones.get(clave);
    }

    /** Marca la sección activa y desliza el indicador; una clave desconocida lo oculta. */
    public void seleccionar(String clave) {
        if (clave != null && clave.equals(seleccion)) {
            return;
        }
        seleccion = botones.containsKey(clave) ? clave : null;
        botones.forEach((k, boton) -> {
            boton.getStyleClass().remove("item-nav-activo");
            if (k.equals(seleccion)) {
                boton.getStyleClass().add("item-nav-activo");
            }
        });
        colocar(true);
    }

    public void alternarContraida() {
        aplicarContraccion(!contraida, true);
        alContraer.accept(contraida);
    }

    private void aplicarContraccion(boolean contraer, boolean animar) {
        contraida = contraer;
        double destino = contraer ? ANCHO_CONTRAIDA : ANCHO_ABIERTA;
        botones.values().forEach(b -> b.setContentDisplay(contraer ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.LEFT));
        titulos.forEach(t -> {
            t.setVisible(!contraer);
            t.setManaged(!contraer);
        });
        marca.setVisible(!contraer);
        marca.setManaged(!contraer);
        if (ancho != null) {
            ancho.stop();
        }
        if (animar) {
            ancho = new Timeline(new KeyFrame(Duration.millis(260),
                    new KeyValue(prefWidthProperty(), destino, Interpolator.EASE_BOTH),
                    new KeyValue(minWidthProperty(), destino, Interpolator.EASE_BOTH),
                    new KeyValue(maxWidthProperty(), destino, Interpolator.EASE_BOTH)));
            ancho.play();
        } else {
            setPrefWidth(destino);
            setMinWidth(destino);
            setMaxWidth(destino);
        }
    }

    private void colocar(boolean animar) {
        Button activo = seleccion == null ? null : botones.get(seleccion);
        if (activo == null) {
            indicador.setVisible(false);
            return;
        }
        lista.applyCss();
        lista.layout();
        double y = activo.getLayoutY();
        double x = activo.getLayoutX();
        double w = activo.getWidth() > 0 ? activo.getWidth() : lista.getWidth();
        boolean primeraVez = !indicador.isVisible();
        indicador.setVisible(true);
        indicador.setMinHeight(ALTO_ELEMENTO);
        indicador.setPrefHeight(ALTO_ELEMENTO);
        indicador.setMaxHeight(ALTO_ELEMENTO);
        if (movimiento != null) {
            movimiento.stop();
        }
        if (animar && !primeraVez) {
            movimiento = new Timeline(new KeyFrame(Duration.millis(380),
                    new KeyValue(indicador.layoutYProperty(), y, Interpolator.SPLINE(0.16, 1, 0.3, 1)),
                    new KeyValue(indicador.layoutXProperty(), x),
                    new KeyValue(indicador.prefWidthProperty(), w),
                    new KeyValue(indicador.minWidthProperty(), w),
                    new KeyValue(indicador.maxWidthProperty(), w)));
            movimiento.play();
        } else {
            indicador.setLayoutX(x);
            indicador.setLayoutY(y);
            indicador.setMinWidth(w);
            indicador.setPrefWidth(w);
            indicador.setMaxWidth(w);
        }
    }
}
