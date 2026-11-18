package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class CatalogoAlgoritmosTest {
    @Test void registraNueveAlgoritmosConNombresUnicosYFabricasValidas() {
        assertEquals(9, CatalogoAlgoritmos.nombres().size());
        assertEquals(9, new HashSet<>(CatalogoAlgoritmos.nombres()).size());
        assertEquals(7, CatalogoAlgoritmos.ordenamientos().size());
        for (var algoritmo : CatalogoAlgoritmos.values()) {
            assertSame(algoritmo, CatalogoAlgoritmos.porNombre(algoritmo.nombre()));
            assertSame(algoritmo, CatalogoAlgoritmos.porComando(algoritmo.comando().toUpperCase(java.util.Locale.ROOT)));
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
