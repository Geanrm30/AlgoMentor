package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PseudocodigoTest {

    @Test void todosLosAlgoritmosTienenPseudocodigo() {
        for (CatalogoAlgoritmos algoritmo : CatalogoAlgoritmos.values()) {
            var p = Pseudocodigo.de(algoritmo).orElseThrow(() -> new AssertionError(algoritmo.nombre()));
            assertTrue(p.lineas().size() >= 4, algoritmo.nombre());
            assertTrue(p.lineaPara(TipoOperacion.FINALIZAR) >= 0, algoritmo.nombre());
        }
    }

    @Test void cadaPasoDeLosOrdenamientosTieneUnaLineaQueResaltar() {
        int[][] entradas = {{5, 3, 8, 1, 2, 9, -4, 3}, {3, 2, 1}, {1, 2, 3, 4}, {40, 7, 7, 120, 0, 15, 22, 3, 3, 61}};
        for (CatalogoAlgoritmos algoritmo : CatalogoAlgoritmos.ordenamientos()) {
            var p = Pseudocodigo.de(algoritmo).orElseThrow();
            for (int[] entrada : entradas) {
                for (Paso paso : algoritmo.crearOrdenamiento().ejecutar(entrada).pasos()) {
                    int linea = p.lineaPara(paso.tipo());
                    assertTrue(linea >= 0 && linea < p.lineas().size(),
                            algoritmo.nombre() + " no resalta " + paso.tipo());
                }
            }
        }
    }

    @Test void cadaPasoDeLosMetodosMatricialesTieneUnaLineaQueResaltar() {
        String datos = new GeneradorEjercicios(5).datosPara("Gauss", 3);
        for (CatalogoAlgoritmos algoritmo : new CatalogoAlgoritmos[]{CatalogoAlgoritmos.GAUSS, CatalogoAlgoritmos.JORDAN}) {
            var p = Pseudocodigo.de(algoritmo).orElseThrow();
            for (Paso paso : algoritmo.crearMatricial().ejecutar(FormatoDatos.leerMatriz(datos)).ejecucion().pasos()) {
                assertTrue(p.lineaPara(paso.tipo()) >= 0, algoritmo.nombre() + " no resalta " + paso.tipo());
            }
        }
    }

    @Test void sinTipoNoHayLineaResaltada() {
        assertEquals(-1, Pseudocodigo.de(CatalogoAlgoritmos.BUBBLE).orElseThrow().lineaPara(null));
        assertEquals(-1, Pseudocodigo.de(CatalogoAlgoritmos.BUBBLE).orElseThrow().lineaPara(TipoOperacion.MEZCLAR));
    }
}
