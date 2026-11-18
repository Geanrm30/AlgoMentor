package ni.edu.uam.algomentor.util;

import ni.edu.uam.algomentor.engine.Fraccion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextoInterfazTest {

    @Test
    void solucionExactaYRespuestaDecimalSePresentanConLaMismaPrecision() {
        String local = TextoInterfaz.solucionDecimal(List.of(Fraccion.de(-35, 3), Fraccion.de(28, 3), Fraccion.de(-2, 3)));
        String externa = TextoInterfaz.solucionExterna("[[-11.666666666666666],[9.333333333333334],[-0.666666666666667]]");
        assertEquals("x1 = -11.666667 · x2 = 9.333333 · x3 = -0.666667", local);
        assertEquals(local, externa);
        assertEquals("x1 = 4 · x2 = -5", TextoInterfaz.solucionDecimal(List.of(Fraccion.de(4), Fraccion.de(-5))));
    }
}
