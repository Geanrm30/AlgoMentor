package ni.edu.uam.algomentor.util;

import ni.edu.uam.algomentor.engine.FormatoDatos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntradaAprendizajeTest {

    @Test
    void leeArregloYRechazaElementosVacios() {
        assertArrayEquals(new int[]{4, -2, 7}, EntradaAprendizaje.leerArreglo("4, -2, 7"));
        assertThrows(IllegalArgumentException.class, () -> EntradaAprendizaje.leerArreglo("4,,7"));
        assertEquals(FormatoDatos.MAX_ELEMENTOS_VISUALES,
                EntradaAprendizaje.leerArreglo("1,".repeat(FormatoDatos.MAX_ELEMENTOS_VISUALES - 1) + "1").length);
        assertThrows(IllegalArgumentException.class, () ->
                EntradaAprendizaje.leerArreglo("1,".repeat(FormatoDatos.MAX_ELEMENTOS_VISUALES) + "1"));
    }

    @Test
    void leeMatrizYRechazaFilasIrregulares() {
        assertEquals("1/2", EntradaAprendizaje.leerMatriz("1/2, 3, 4\n2, 1, 0")[0][0].toString());
        assertThrows(IllegalArgumentException.class, () -> EntradaAprendizaje.leerMatriz("1,2,3\n4,5"));
    }
}
