package ni.edu.uam.algomentor.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComandosTerminalTest {

    @Test
    void ejecutaSoloComandosEducativos() {
        assertTrue(ComandosTerminal.ejecutar("ordenar bubble 3,1,2").contains("[1, 2, 3]"));
        assertTrue(ComandosTerminal.ejecutar("resolver jordan 1,1,3;1,-1,1").contains("Una solución"));
        assertThrows(IllegalArgumentException.class, () -> ComandosTerminal.ejecutar("rm -rf /"));
    }
}
