package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FraccionTest {

    @Test
    void siempreQuedaReducidaYConDenominadorPositivo() {
        assertEquals(Fraccion.de(1, 2), Fraccion.de(2, 4));
        assertEquals("-1/2", Fraccion.de(1, -2).toString());
        assertEquals("0", Fraccion.de(0, 5).toString());
        assertEquals("3", Fraccion.de(6, 2).toString());
    }

    @Test
    void sumaRestaMultiplicaYDivide() {
        assertEquals(Fraccion.de(5, 6), Fraccion.de(1, 2).sumar(Fraccion.de(1, 3)));
        assertEquals(Fraccion.de(1, 6), Fraccion.de(1, 2).restar(Fraccion.de(1, 3)));
        assertEquals(Fraccion.de(1, 2), Fraccion.de(2, 3).multiplicar(Fraccion.de(3, 4)));
        assertEquals(Fraccion.de(2), Fraccion.de(1, 2).dividir(Fraccion.de(1, 4)));
    }

    @Test
    void noPierdePrecisionComoUnDouble() {
        Fraccion tercio = Fraccion.de(1, 3);
        assertEquals(Fraccion.UNO, tercio.sumar(tercio).sumar(tercio));
    }

    @Test
    void divisionEntreCeroLanzaExcepcion() {
        assertThrows(ArithmeticException.class, () -> Fraccion.de(1).dividir(Fraccion.CERO));
        assertThrows(ArithmeticException.class, () -> Fraccion.de(1, 0));
        assertThrows(ArithmeticException.class, () -> Fraccion.CERO.reciproco());
    }

    @Test
    void leeEnterosYFracciones() {
        assertEquals(Fraccion.de(3, 4), Fraccion.leer("3/4"));
        assertEquals(Fraccion.de(-2), Fraccion.leer("-2"));
        assertEquals(Fraccion.de(1, 2), Fraccion.leer(" 5/10 "));
        assertEquals(Fraccion.de(-1, 3), Fraccion.leer("1/-3"));
    }

    @Test
    void rechazaTextoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> Fraccion.leer("a/b"));
        assertThrows(IllegalArgumentException.class, () -> Fraccion.leer("1/0"));
        assertThrows(IllegalArgumentException.class, () -> Fraccion.leer(""));
        assertThrows(IllegalArgumentException.class, () -> Fraccion.leer(null));
    }

    @Test
    void comparaYNiega() {
        assertTrue(Fraccion.de(1, 3).compareTo(Fraccion.de(1, 2)) < 0);
        assertTrue(Fraccion.de(-1, 2).compareTo(Fraccion.de(1, 3)) < 0);
        assertEquals(Fraccion.de(-3, 4), Fraccion.de(3, 4).negar());
        assertEquals(Fraccion.de(4, 3), Fraccion.de(3, 4).reciproco());
        assertTrue(Fraccion.CERO.esCero());
    }
}
