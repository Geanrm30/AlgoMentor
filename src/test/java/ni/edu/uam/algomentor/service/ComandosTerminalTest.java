package ni.edu.uam.algomentor.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComandosTerminalTest {

    @Test void aceptaLosNuevosComandosYLosPublicaEnAyuda() {
        for (var algoritmo : ni.edu.uam.algomentor.engine.CatalogoAlgoritmos.ordenamientos()) {
            assertTrue(ComandosTerminal.ejecutar("ayuda").contains(algoritmo.comando()));
            assertTrue(ComandosTerminal.ejecutar("ordenar " + algoritmo.comando() + " 3,-1,0,3")
                    .contains("[-1, 0, 3, 3]"));
        }
        assertThrows(IllegalArgumentException.class, () -> ComandosTerminal.ejecutar("ordenar gauss 1,2"));
        assertThrows(IllegalArgumentException.class, () -> ComandosTerminal.ejecutar("resolver quick 1,2"));
    }

    @Test
    void ejecutaSoloComandosEducativos() {
        assertTrue(ComandosTerminal.ejecutar("ordenar bubble 3,1,2").contains("[1, 2, 3]"));
        assertTrue(ComandosTerminal.ejecutar("resolver jordan 1,1,3;1,-1,1").contains("Una solución"));
        assertThrows(IllegalArgumentException.class, () -> ComandosTerminal.ejecutar("rm -rf /"));
    }
}
