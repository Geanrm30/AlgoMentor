package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class CatalogoAlgoritmosTest {
    @Test void registraQuinceAlgoritmosConNombresUnicosYFabricasValidas() {
        assertEquals(15, CatalogoAlgoritmos.nombres().size());
        assertEquals(15, new HashSet<>(CatalogoAlgoritmos.nombres()).size());
        assertEquals(13, CatalogoAlgoritmos.ordenamientos().size());
        for (var algoritmo : CatalogoAlgoritmos.values()) {
            assertSame(algoritmo, CatalogoAlgoritmos.porNombre(algoritmo.nombre()));
            assertFalse(algoritmo.etiqueta().isBlank());
            if (algoritmo.esOrdenamiento()) assertEquals(algoritmo.nombre(), algoritmo.crearOrdenamiento().nombre());
            else assertNotNull(algoritmo.crearMatricial());
        }
    }

    @Test void rechazaSeleccionInvalidaSinElegirOtroAlgoritmoPorDefecto() {
        assertFalse(CatalogoAlgoritmos.admite(null));
        assertThrows(IllegalArgumentException.class, () -> CatalogoAlgoritmos.porNombre("Desconocido"));
        assertThrows(IllegalArgumentException.class, () -> CatalogoAlgoritmos.GAUSS.crearOrdenamiento());
        assertThrows(IllegalArgumentException.class, () -> CatalogoAlgoritmos.QUICK.crearMatricial());
    }
}
