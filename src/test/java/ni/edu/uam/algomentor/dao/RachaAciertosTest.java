package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.dao.EstadisticasDAO.IntentoReciente;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RachaAciertosTest {

    private static IntentoReciente intento(Boolean correcto) {
        return new IntentoReciente(1L, LocalDateTime.of(2026, 11, 22, 12, 0),
                "Bubble Sort", "PRACTICA", correcto, 30);
    }

    @Test
    void sinIntentosLaRachaEsCero() {
        assertEquals(0, EstadisticasDAO.rachaActual(List.of()));
    }

    @Test
    void cuentaLosAciertosSeguidosMasRecientes() {
        assertEquals(3, EstadisticasDAO.rachaActual(
                List.of(intento(true), intento(true), intento(true), intento(false), intento(true))));
    }

    @Test
    void unErrorRecienteReiniciaLaRacha() {
        assertEquals(0, EstadisticasDAO.rachaActual(List.of(intento(false), intento(true))));
    }

    @Test
    void ignoraLosIntentosSinCalificar() {
        assertEquals(2, EstadisticasDAO.rachaActual(
                List.of(intento(true), intento(null), intento(true), intento(false))));
    }

    @Test
    void laPrecisionAcumuladaSeCalculaDelMasAntiguoAlMasReciente() {
        // Del más nuevo al más antiguo: error, acierto, sin calificar, acierto.
        List<Double> serie = EstadisticasDAO.precisionAcumulada(
                List.of(intento(false), intento(true), intento(null), intento(true)));
        assertEquals(List.of(100.0, 100.0, (2 * 100.0) / 3), serie);
    }

    @Test
    void laPrecisionAcumuladaSinIntentosCalificadosEstaVacia() {
        assertEquals(List.of(), EstadisticasDAO.precisionAcumulada(List.of(intento(null))));
    }

    @Test
    void elRendimientoPorAlgoritmoCalculaElPorcentajeSoloConCalificados() {
        EstadisticasDAO.RendimientoAlgoritmo rendimiento =
                new EstadisticasDAO.RendimientoAlgoritmo("Quick Sort", 10, 3, 1);
        assertEquals(75.0, rendimiento.porcentajeAciertos(), 0.0001);
        assertEquals(0.0,
                new EstadisticasDAO.RendimientoAlgoritmo("Quick Sort", 2, 0, 0).porcentajeAciertos(), 0.0001);
    }
}
