
package ni.edu.uam.algomentor.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadisticasDAOTest {

    @Test
    void debeAceptarPerfilValido() {
        assertDoesNotThrow(() ->
                EstadisticasDAO.validarPerfilId(1L)
        );
    }

    @Test
    void debeRechazarPerfilNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                EstadisticasDAO.validarPerfilId(null)
        );
    }

    @Test
    void debeRechazarPerfilConIdInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                EstadisticasDAO.validarPerfilId(0L)
        );

        assertThrows(IllegalArgumentException.class, () ->
                EstadisticasDAO.validarPerfilId(-5L)
        );
    }

    @Test
    void debeCalcularPorcentajeDeAciertos() {
        var estadistica =
                new EstadisticasDAO.EstadisticaHabilidad(
                        1L, "Aritmetica", 15, 5
                );

        assertEquals(20, estadistica.totalEvaluaciones());

        assertEquals(
                75.0,
                estadistica.porcentajeAciertos(),
                0.001
        );
    }

    @Test
    void debeManejarHabilidadSinEvaluaciones() {
        var estadistica =
                new EstadisticasDAO.EstadisticaHabilidad(
                        1L, "Fracciones", 0, 0
                );

        assertEquals(0, estadistica.totalEvaluaciones());
        assertEquals(0.0, estadistica.porcentajeAciertos());
    }

    @Test
    void debeRechazarIdHabilidadInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                new EstadisticasDAO.EstadisticaHabilidad(
                        -1L, "Signos", 5, 2
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                new EstadisticasDAO.EstadisticaHabilidad(
                        null, "Signos", 5, 2
                )
        );
    }

    @Test
    void debeRechazarNombreHabilidadVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new EstadisticasDAO.EstadisticaHabilidad(
                        1L, "  ", 5, 2
                )
        );
    }

    @Test
    void debeRechazarContadoresNegativos() {
        assertThrows(IllegalArgumentException.class, () ->
                new EstadisticasDAO.EstadisticaHabilidad(
                        1L, "Signos", -1, 2
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                new EstadisticasDAO.EstadisticaHabilidad(
                        1L, "Signos", 5, -2
                )
        );
    }

    @Test
    void debeCalcularResumenDeIntentos() {
        var resumen =
                new EstadisticasDAO.ResumenIntentos(
                        10, 6, 3
                );

        assertEquals(10, resumen.total());
        assertEquals(6, resumen.correctos());
        assertEquals(3, resumen.incorrectos());
        assertEquals(1, resumen.sinCalificar());

        assertEquals(
                60.0,
                resumen.porcentajeCorrectos(),
                0.001
        );
    }

    @Test
    void debeManejarResumenSinIntentos() {
        var resumen =
                new EstadisticasDAO.ResumenIntentos(
                        0, 0, 0
                );

        assertEquals(0, resumen.sinCalificar());
        assertEquals(0.0, resumen.porcentajeCorrectos());
    }

    @Test
    void debeCalcularDominioPorAlgoritmoYManejarCeroIntentos() {
        var conIntentos = new EstadisticasDAO.DominioAlgoritmo(5, 4);
        var sinIntentos = new EstadisticasDAO.DominioAlgoritmo(0, 0);

        assertEquals(80.0, conIntentos.porcentajeAciertos());
        assertEquals(0.0, sinIntentos.porcentajeAciertos());
    }

    @Test
    void debeRechazarContadoresInvalidosDeDominio() {
        assertThrows(IllegalArgumentException.class,
                () -> new EstadisticasDAO.DominioAlgoritmo(-1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new EstadisticasDAO.DominioAlgoritmo(2, 3));
    }

    @Test
    void debeRechazarTotalNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new EstadisticasDAO.ResumenIntentos(
                        -1, 0, 0
                )
        );
    }

    @Test
    void debeRechazarResultadosMayoresAlTotal() {
        assertThrows(IllegalArgumentException.class, () ->
                new EstadisticasDAO.ResumenIntentos(
                        5, 4, 3
                )
        );
    }
}
