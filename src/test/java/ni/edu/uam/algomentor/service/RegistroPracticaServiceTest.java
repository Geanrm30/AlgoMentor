package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.TipoError;
import ni.edu.uam.algomentor.engine.TipoOperacion;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegistroPracticaServiceTest {

    @Test
    void pasaErrorDeSignoDesdeElVerificadorAlRegistro() {
        var evaluacion = new EvaluadorPasoMatriz().evaluar("1,1,3\n1,-1,1",
                TipoOperacion.ELIMINAR_FILA, "2", "1", "1", "1,1,3\n0,-2,2");
        Map<TipoError, Long> ids = Map.of(TipoError.SIGNO, 11L, TipoError.OPERACION_INVALIDA, 12L);
        var paso = RegistroPracticaService.construirPaso("matriz del estudiante", evaluacion.correcto(),
                evaluacion.errores(), ids::get);

        assertFalse(paso.correcto());
        assertEquals(11L, paso.habilidadEvaluadaId());
        assertEquals(1, paso.errores().size());
        assertEquals(TipoError.SIGNO, paso.errores().getFirst().tipo());
        assertEquals(11L, paso.errores().getFirst().habilidadId());
        assertEquals("Signos", HabilidadErrorResolver.nombreHabilidad(paso.errores().getFirst().tipo()));
    }

    @Test
    void pideCargarElSeedCuandoFaltaUnaHabilidad() {
        var error = new ni.edu.uam.algomentor.engine.ErrorOperacion(TipoError.FRACCION, 0, 0, "Error");
        var fallo = assertThrows(IllegalStateException.class,
                () -> RegistroPracticaService.construirPaso("x", false, java.util.List.of(error),
                        tipo -> { throw new IllegalStateException("No existe la habilidad"); }));
        assertTrue(fallo.getMessage().contains("schema.sql"));
    }
}
