package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AlgoritmosExtraTest {

    @Test void todosFuncionanConSuEntradaDeEjemplo() {
        for (AlgoritmosExtra algoritmo : AlgoritmosExtra.values()) {
            ResultadoExtra r = algoritmo.ejecutar(algoritmo.entradaEjemplo());
            assertFalse(r.pasos().isEmpty(), algoritmo.name());
            assertFalse(r.conclusion().isBlank());
            for (int i = 0; i < r.pasos().size(); i++) {
                assertEquals(i + 1, r.pasos().get(i).numero());
                assertFalse(r.pasos().get(i).explicacion().isBlank());
                assertNotNull(r.pasos().get(i).estado());
            }
        }
    }

    @Test void busquedaLinealEncuentraOInformaQueNoEsta() {
        assertTrue(AlgoritmosExtra.BUSQUEDA_LINEAL.ejecutar("7, 3, 9, 1, 5 ; 9").conclusion().contains("posición 3"));
        assertTrue(AlgoritmosExtra.BUSQUEDA_LINEAL.ejecutar("7, 3, 9 ; 4").conclusion().startsWith("No está"));
    }

    @Test void busquedaBinariaOrdenaSiHaceFaltaYUsaLogaritmoDePasos() {
        var r = AlgoritmosExtra.BUSQUEDA_BINARIA.ejecutar("9, 1, 5, 3 ; 5");
        assertTrue(r.pasos().get(0).explicacion().contains("se ordena primero"));
        assertTrue(r.conclusion().contains("posición 3"));
        var grande = AlgoritmosExtra.BUSQUEDA_BINARIA.ejecutar("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16 ; 16");
        assertTrue(grande.pasos().size() <= 5);
        assertTrue(AlgoritmosExtra.BUSQUEDA_BINARIA.ejecutar("1, 3, 5 ; 4").conclusion().startsWith("No está"));
    }

    @Test void recorridosDeGrafoSonDeterministas() {
        assertEquals("Orden de visita: A, B, C, D, E, F.", AlgoritmosExtra.BFS.ejecutar("A").conclusion());
        assertEquals("Orden de visita: A, B, C, E, D, F.", AlgoritmosExtra.DFS.ejecutar("a").conclusion());
        assertTrue(AlgoritmosExtra.BFS.ejecutar("F").conclusion().startsWith("Orden de visita: F"));
    }

    @Test void dijkstraCalculaLasDistanciasMinimas() {
        assertEquals("Distancias desde A: A=0, B=3, C=2, D=8, E=10, F=13.", AlgoritmosExtra.DIJKSTRA.ejecutar("A").conclusion());
    }

    @Test void fibonacciYHanoi() {
        assertTrue(AlgoritmosExtra.FIBONACCI.ejecutar("10").conclusion().startsWith("F(10) = 55."));
        assertTrue(AlgoritmosExtra.FIBONACCI.ejecutar("10").conclusion().contains("177 llamadas"));
        assertTrue(AlgoritmosExtra.FIBONACCI.ejecutar("0").conclusion().startsWith("F(0) = 0."));
        for (int n = 1; n <= 6; n++) {
            var r = AlgoritmosExtra.HANOI.ejecutar(Integer.toString(n));
            assertEquals((1 << n), r.pasos().size());
            var fin = (EstadoVisual.Torres) r.pasos().getLast().estado();
            assertEquals(n, fin.varillas().get(2).size());
            assertTrue(fin.varillas().get(0).isEmpty() && fin.varillas().get(1).isEmpty());
        }
    }

    @Test void hanoiNuncaPoneUnDiscoGrandeSobreUnoPequeno() {
        for (PasoExtra paso : AlgoritmosExtra.HANOI.ejecutar("5").pasos()) {
            for (var varilla : ((EstadoVisual.Torres) paso.estado()).varillas()) {
                for (int i = 1; i < varilla.size(); i++) assertTrue(varilla.get(i - 1) > varilla.get(i));
            }
        }
    }

    @Test void rechazaEntradasInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> AlgoritmosExtra.BFS.ejecutar("Z"));
        assertThrows(IllegalArgumentException.class, () -> AlgoritmosExtra.FIBONACCI.ejecutar("31"));
        assertThrows(IllegalArgumentException.class, () -> AlgoritmosExtra.FIBONACCI.ejecutar("x"));
        assertThrows(IllegalArgumentException.class, () -> AlgoritmosExtra.HANOI.ejecutar("0"));
        assertThrows(IllegalArgumentException.class, () -> AlgoritmosExtra.BUSQUEDA_LINEAL.ejecutar("1,2,3"));
        assertThrows(IllegalArgumentException.class, () -> AlgoritmosExtra.BUSQUEDA_LINEAL.ejecutar("1,a ; 2"));
        assertThrows(IllegalArgumentException.class, () -> AlgoritmosExtra.BUSQUEDA_LINEAL.ejecutar(" "));
        assertThrows(IllegalArgumentException.class, () -> AlgoritmosExtra.BUSQUEDA_LINEAL.ejecutar(null));
    }
}
