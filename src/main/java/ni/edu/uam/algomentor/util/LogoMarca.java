package ni.edu.uam.algomentor.util;

import javafx.scene.Group;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.List;

/** Marca de AlgoMentor: un cuadrado partido en dos piezas inclinadas, con degradado de cian a violeta. */
public final class LogoMarca {

    public static final String CLAVE_PIEZAS = "algomentor.piezasLogo";

    private LogoMarca() { }

    public static Group crear(double lado) {
        LinearGradient superior = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#67e8f9")), new Stop(1, Color.web("#38bdf8")));
        LinearGradient inferior = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#818cf8")), new Stop(1, Color.web("#8b5cf6")));
        Polygon arriba = new Polygon(0.14 * lado, 0, lado, 0, 0.86 * lado, 0.46 * lado, 0, 0.46 * lado);
        Polygon abajo = new Polygon(0.14 * lado, 0.54 * lado, lado, 0.54 * lado, 0.86 * lado, lado, 0, lado);
        arriba.setFill(superior);
        abajo.setFill(inferior);
        Group marca = new Group(arriba, abajo);
        marca.getProperties().put(CLAVE_PIEZAS, List.of(arriba, abajo));
        marca.setMouseTransparent(true);
        return marca;
    }
}
