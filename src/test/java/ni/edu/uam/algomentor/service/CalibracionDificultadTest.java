package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.DificultadRealDAO.EstadisticaEjercicio;
import org.junit.jupiter.api.Test;

import static ni.edu.uam.algomentor.service.CalibracionDificultad.Diagnostico.*;
import static org.junit.jupiter.api.Assertions.*;

class CalibracionDificultadTest {

    private static EstadisticaEjercicio ejercicio(int nivel, long intentos, long aciertos) {
        return new EstadisticaEjercicio(1L, "Gauss", nivel, intentos, aciertos, intentos - aciertos);
    }

    @Test
    void sinIntentosONueveIntentosLosDatosSonInsuficientes() {
        for (int nivel = 1; nivel <= 5; nivel++) {
            assertEquals(DATOS_INSUFICIENTES, CalibracionDificultad.clasificar(ejercicio(nivel, 0, 0)));
            assertEquals(DATOS_INSUFICIENTES, CalibracionDificultad.clasificar(ejercicio(nivel, 9, 9)));
            assertEquals(DATOS_INSUFICIENTES, CalibracionDificultad.clasificar(ejercicio(nivel, 9, 0)));
        }
    }

    @Test
    void diezIntentosYaPermitenDiagnosticar() {
        assertEquals(BIEN_CALIBRADO, CalibracionDificultad.clasificar(ejercicio(1, 10, 7)));
        assertEquals(MAL_CALIBRADO, CalibracionDificultad.clasificar(ejercicio(1, 10, 6)));
    }

    @Test
    void losExtremosDeCadaRangoSonInclusivos() {
        int[][] rangos = {{1, 70, 100}, {2, 55, 85}, {3, 35, 65}, {4, 15, 45}, {5, 0, 30}};
        for (int[] rango : rangos) {
            for (int aciertos : new int[]{rango[1], (rango[1] + rango[2]) / 2, rango[2]}) {
                assertEquals(BIEN_CALIBRADO,
                        CalibracionDificultad.clasificar(ejercicio(rango[0], 100, aciertos)),
                        "Nivel " + rango[0] + ", acierto " + aciertos + "%");
            }
        }
    }

    @Test
    void fueraDeLosRangosHayMalaCalibracion() {
        int[][] casos = {{1, 69}, {2, 54}, {2, 86}, {3, 34}, {3, 66}, {4, 14}, {4, 46}, {5, 31}};
        for (int[] caso : casos) {
            assertEquals(MAL_CALIBRADO, CalibracionDificultad.clasificar(ejercicio(caso[0], 100, caso[1])),
                    "Nivel " + caso[0] + ", acierto " + caso[1] + "%");
        }
    }

    @Test
    void elRedondeoVisualNoCambiaElDiagnostico() {
        var porDebajo = ejercicio(1, 10_000, 6_999);
        var porEncima = ejercicio(2, 10_000, 8_501);

        assertEquals("70.0%", FilasInforme.porcentaje(porDebajo.porcentajeAciertos()));
        assertEquals("85.0%", FilasInforme.porcentaje(porEncima.porcentajeAciertos()));
        assertEquals(MAL_CALIBRADO, CalibracionDificultad.clasificar(porDebajo));
        assertEquals(MAL_CALIBRADO, CalibracionDificultad.clasificar(porEncima));
    }

    @Test
    void calculaPorcentajesSinDivisionEnteraNiDesbordamiento() {
        assertEquals(0.0, ejercicio(1, 0, 0).porcentajeAciertos());
        assertEquals(0.0, ejercicio(5, 10, 0).porcentajeAciertos());
        assertEquals(100.0, ejercicio(1, 10, 10).porcentajeAciertos());
        assertEquals(200.0 / 3, ejercicio(3, 3, 2).porcentajeAciertos(), 0.000001);
        assertEquals(100.0, ejercicio(1, Long.MAX_VALUE, Long.MAX_VALUE).porcentajeAciertos());
    }

    @Test
    void rechazaNivelesYContadoresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> ejercicio(0, 10, 5));
        assertThrows(IllegalArgumentException.class, () -> ejercicio(6, 10, 5));
        assertThrows(IllegalArgumentException.class, () -> ejercicio(1, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> ejercicio(1, 10, -1));
        assertThrows(IllegalArgumentException.class, () -> ejercicio(1, 10, 11));
        assertThrows(IllegalArgumentException.class,
                () -> new EstadisticaEjercicio(1L, "Gauss", 1, 10, 5, 4));
    }
}
