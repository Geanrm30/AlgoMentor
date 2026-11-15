
package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.TipoError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HabilidadErrorResolverTest {

    @Test
    void debeRelacionarErrorDeSigno() {
        assertEquals(
                "Signos",
                HabilidadErrorResolver.nombreHabilidad(
                        TipoError.SIGNO
                )
        );
    }

    @Test
    void debeRelacionarErrorDeFraccion() {
        assertEquals(
                "Fracciones",
                HabilidadErrorResolver.nombreHabilidad(
                        TipoError.FRACCION
                )
        );
    }

    @Test
    void debeRelacionarErrorAritmetico() {
        assertEquals(
                "Aritmetica",
                HabilidadErrorResolver.nombreHabilidad(
                        TipoError.ARITMETICO
                )
        );
    }

    @Test
    void debeRelacionarOperacionInvalida() {
        assertEquals(
                "Procedimiento",
                HabilidadErrorResolver.nombreHabilidad(
                        TipoError.OPERACION_INVALIDA
                )
        );
    }

    @Test
    void debeRechazarTipoErrorNulo() {
        assertThrows(
                NullPointerException.class,
                () -> HabilidadErrorResolver.nombreHabilidad(
                        null
                )
        );
    }
}
