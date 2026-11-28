package ni.edu.uam.algomentor.util;

import javafx.scene.Group;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/** Íconos vectoriales: cada nombre corresponde a una forma {@code .ico-nombre} definida en el CSS. */
public final class Iconos {

    public static final String CLAVE_TAPA = "algomentor.tapaPapelera";

    private Iconos() { }

    public static Region crear(String nombre) {
        Region icono = new Region();
        icono.getStyleClass().addAll("ico", "ico-" + nombre);
        icono.setMouseTransparent(true);
        return icono;
    }

    /** Ícono pequeño (16 px) para usar dentro de un botón. */
    public static Region crearBoton(String nombre) {
        Region icono = crear(nombre);
        icono.getStyleClass().add("ico-boton");
        return icono;
    }

    /**
     * Papelera de dos piezas dibujadas en su espacio de 24 x 24 y reducidas a 16 px; la tapa queda en las
     * propiedades para poder abrirla y cerrarla.
     */
    public static Group papelera() {
        Region cuerpo = crearBoton("papelera-cuerpo");
        Region tapa = crearBoton("papelera-tapa");
        StackPane piezas = new StackPane(cuerpo, tapa);
        piezas.setMinSize(24, 24);
        piezas.setPrefSize(24, 24);
        piezas.setMaxSize(24, 24);
        piezas.setScaleX(16.0 / 24);
        piezas.setScaleY(16.0 / 24);
        Group papelera = new Group(piezas);
        papelera.setMouseTransparent(true);
        papelera.getProperties().put(CLAVE_TAPA, tapa);
        return papelera;
    }
}
