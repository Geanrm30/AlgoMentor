package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.TipoError;
import ni.edu.uam.algomentor.engine.TipoOperacion;
import ni.edu.uam.algomentor.engine.ErrorOperacion;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;

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
        var error = new ErrorOperacion(TipoError.FRACCION, 0, 0, "Error");
        var fallo = assertThrows(IllegalStateException.class,
                () -> RegistroPracticaService.construirPaso("x", false, List.of(error),
                        tipo -> { throw new IllegalStateException("No existe la habilidad: Fracciones"); }));
        assertTrue(fallo.getMessage().contains("schema.sql"));
    }

    @Test
    void noConfundeUnaFallaDeConexionConDatosInicialesAusentes() {
        var error = new ErrorOperacion(TipoError.FRACCION, 0, 0, "Error");
        var fallo = assertThrows(IllegalStateException.class,
                () -> RegistroPracticaService.construirPaso("x", false, List.of(error),
                        tipo -> { throw new IllegalStateException("Falta db.url"); }));
        assertEquals("Falta db.url", fallo.getMessage());
    }

    @Test
    void reparteUnaEvaluacionEntreHabilidadesDistintas() {
        var errores = List.of(new ErrorOperacion(TipoError.SIGNO, 0, 0, "Signo"),
                new ErrorOperacion(TipoError.FRACCION, 0, 1, "Fracción"));
        var paso = RegistroPracticaService.construirPaso("x", false, errores,
                Map.of(TipoError.SIGNO, 11L, TipoError.FRACCION, 12L)::get);
        var progreso = IntentoService.calcularResultados("PRACTICA", List.of(paso));
        assertEquals(1, progreso.get(11L).errores());
        assertEquals(1, progreso.get(12L).errores());
    }
}
