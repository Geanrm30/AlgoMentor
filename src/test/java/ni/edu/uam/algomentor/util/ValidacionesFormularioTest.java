package ni.edu.uam.algomentor.util;

import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.engine.FormatoDatos;
import ni.edu.uam.algomentor.model.Algoritmo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidacionesFormularioTest {
    @Test void bibliotecaEImportadorAceptanDatosValidosDelCatalogo() {
        for (var registro : CatalogoAlgoritmos.ordenamientos()) {
            Algoritmo algoritmo = algoritmo(registro.nombre());
            assertDoesNotThrow(() -> ValidacionesFormulario.validarEjercicio(algoritmo, "MANUAL", 2, "-3,0,2,-3"));
            assertDoesNotThrow(() -> ValidacionesFormulario.validarEjercicio(algoritmo, "ALEATORIO", 2,
                    "v1;ARREGLO;-2147483648,2147483647"));
            assertThrows(IllegalArgumentException.class, () -> ValidacionesFormulario.validarEjercicio(
                    algoritmo, "MANUAL", 1, "abc"));
            assertThrows(IllegalArgumentException.class, () -> ValidacionesFormulario.validarEjercicio(
                    algoritmo, "MANUAL", 1, "v1;ARREGLO;" + "1,".repeat(FormatoDatos.MAX_ELEMENTOS_VISUALES) + "1"));
        }
    }

    @Test void seleccionesVaciasProducenErroresDeValidacion() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidacionesFormulario.validarAlgoritmo("Quick Sort", null, "", ""));
        assertThrows(IllegalArgumentException.class,
                () -> ValidacionesFormulario.validarEjercicio(algoritmo("Quick Sort"), null, 1, "1,2"));
    }

    private Algoritmo algoritmo(String nombre) {
        Algoritmo algoritmo = new Algoritmo();
        algoritmo.setNombre(nombre);
        return algoritmo;
    }
}
