package ni.edu.uam.algomentor.util;

import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TutorialTest {

    @Test void colocaLaTarjetaALaDerechaSiCabe() {
        Point2D p = Tutorial.ubicarTarjeta(new BoundingBox(50, 100, 200, 80), 360, 200, 1200, 800);
        assertEquals(268, p.getX(), 0.001);
        assertEquals(100, p.getY(), 0.001);
    }

    @Test void siNoCabeALaDerechaLaPoneALaIzquierda() {
        Point2D p = Tutorial.ubicarTarjeta(new BoundingBox(800, 100, 300, 80), 360, 200, 1200, 800);
        assertEquals(800 - 18 - 360, p.getX(), 0.001);
    }

    @Test void siElElementoOcupaTodoElAnchoLaPoneDebajoOEncima() {
        Point2D debajo = Tutorial.ubicarTarjeta(new BoundingBox(0, 0, 1200, 60), 360, 200, 1200, 800);
        assertEquals(78, debajo.getY(), 0.001);
        Point2D encima = Tutorial.ubicarTarjeta(new BoundingBox(0, 700, 1200, 100), 360, 200, 1200, 800);
        assertTrue(encima.getY() + 200 <= 800);
    }

    @Test void nuncaSeSaleDeLaVentana() {
        for (double x = -50; x < 1300; x += 150) {
            for (double y = -50; y < 900; y += 150) {
                Point2D p = Tutorial.ubicarTarjeta(new BoundingBox(x, y, 160, 90), 360, 220, 900, 600);
                assertTrue(p.getX() >= 12 && p.getX() + 360 <= 900 - 12, "x " + p);
                assertTrue(p.getY() >= 12 && p.getY() + 220 <= 600 - 12, "y " + p);
            }
        }
    }
}
