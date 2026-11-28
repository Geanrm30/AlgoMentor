package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class FichaAlgoritmoTest {
    @Test void cadaOrdenamientoTieneUnaFichaCompletaYUnica() {
        assertEquals(CatalogoAlgoritmos.ordenamientos().size(), FichaAlgoritmo.todas().size());
        assertEquals(FichaAlgoritmo.todas().size(),
                new HashSet<>(FichaAlgoritmo.todas().stream().map(FichaAlgoritmo::algoritmo).toList()).size());
        for (CatalogoAlgoritmos algoritmo : CatalogoAlgoritmos.ordenamientos()) {
            FichaAlgoritmo ficha = FichaAlgoritmo.de(algoritmo).orElseThrow(() -> new AssertionError(algoritmo.nombre()));
            assertFalse(ficha.mecanica().isBlank());
            assertFalse(ficha.tecnica().isBlank());
            assertFalse(ficha.cuandoUsarlo().isBlank());
            assertFalse(ficha.mejor().isBlank() || ficha.promedio().isBlank() || ficha.peor().isBlank());
        }
    }

    @Test void losMetodosDeSistemasLinealesNoTienenFicha() {
        assertTrue(FichaAlgoritmo.de(CatalogoAlgoritmos.GAUSS).isEmpty());
    }

    @Test void estabilidadCoincideConLosAlgoritmosConocidos() {
        assertTrue(FichaAlgoritmo.de(CatalogoAlgoritmos.MERGE).orElseThrow().estable());
        assertFalse(FichaAlgoritmo.de(CatalogoAlgoritmos.HEAP).orElseThrow().estable());
        assertFalse(FichaAlgoritmo.de(CatalogoAlgoritmos.MERGE).orElseThrow().enSitio());
    }
}
