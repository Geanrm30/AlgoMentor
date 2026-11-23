package ni.edu.uam.algomentor.util;

import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvHistorialTest {

    @Test
    void generaCabeceraYUnaFilaPorIntento() {
        String csv = CsvHistorial.generar(List.of(
                new EstadisticasDAO.IntentoReciente(1L, LocalDateTime.of(2026, 11, 22, 18, 9, 5),
                        "Gauss", "PRACTICA", true, 30),
                new EstadisticasDAO.IntentoReciente(2L, LocalDateTime.of(2026, 11, 22, 18, 10, 0),
                        "Gauss", "SIMULACION", null, null)));

        String[] lineas = csv.split("\r\n");
        assertEquals(3, lineas.length);
        assertEquals("Fecha,Algoritmo,Modo,Resultado,Duración (segundos)", lineas[0]);
        assertTrue(lineas[1].startsWith("2026-11-22 18:09:05,"));
        assertTrue(lineas[1].endsWith(",Práctica,Correcto,30"));
        assertTrue(lineas[2].endsWith(",Simulación,Sin calificar,"));
    }

    @Test
    void escapaComasYComillas() {
        assertEquals("\"a,b\"", CsvHistorial.celda("a,b"));
        assertEquals("\"di \"\"hola\"\"\"", CsvHistorial.celda("di \"hola\""));
    }

    @Test
    void neutralizaLasFormulas() {
        assertEquals("'=SUMA(A1)", CsvHistorial.celda("=SUMA(A1)"));
        assertEquals("'@usuario", CsvHistorial.celda("@usuario"));
        assertEquals("'-1", CsvHistorial.celda("-1"));
        assertEquals("", CsvHistorial.celda(null));
    }
}
