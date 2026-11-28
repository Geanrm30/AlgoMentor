package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.engine.FichaAlgoritmo;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RetosServiceTest {

    // ---------- Desafío ----------

    @Test void laRespuestaEsperadaCoincideSinImportarElFormato() {
        assertTrue(DesafioService.coincide("[3, 1, 2]", "3,1,2"));
        assertTrue(DesafioService.coincide("[3, 1, 2]", " [3 , 1 ,2] "));
        assertTrue(DesafioService.coincide("[-3, 1]", "-3 1"));
        assertFalse(DesafioService.coincide("[3, 1, 2]", "3, 2, 1"));
        assertFalse(DesafioService.coincide("[3, 1, 2]", ""));
        assertFalse(DesafioService.coincide("[3, 1, 2]", null));
        assertFalse(DesafioService.coincide("[3, 1, 2]", "3, 1"));
    }

    @Test void lasPreguntasSonReproduciblesYTienenRespuestaValida() {
        for (int ronda = 0; ronda < 12; ronda++) {
            var a = new DesafioService(7).nuevaPregunta(ronda);
            var b = new DesafioService(7).nuevaPregunta(ronda);
            assertEquals(a, b);
            assertTrue(a.esperada().startsWith("[") && a.esperada().endsWith("]"));
            assertTrue(a.enunciado().contains("cambio número"));
        }
        var juego = new DesafioService(3);
        for (int i = 0; i < 200; i++) {
            var p = juego.nuevaPregunta(i % 12);
            assertTrue(DesafioService.coincide(p.esperada(), p.esperada()));
        }
    }

    @Test void elPuntajeSumaRachaYSeReiniciaAlFallar() {
        var juego = new DesafioService(1);
        var p = juego.nuevaPregunta(0);
        assertTrue(juego.responder(p, p.esperada()));
        assertEquals(10, juego.puntos());
        assertTrue(juego.responder(p, p.esperada()));
        assertEquals(10 + 12, juego.puntos());
        assertFalse(juego.responder(p, "999"));
        assertEquals(0, juego.racha());
        assertEquals(2, juego.mejorRacha());
        assertEquals(1, juego.errores());
        assertTrue(juego.responder(p, p.esperada()));
        assertEquals(22 + 10, juego.puntos());
    }

    // ---------- Cuestionario ----------

    @Test void todasLasPreguntasGeneradasSonValidas() {
        for (long semilla = 0; semilla < 150; semilla++) {
            var preguntas = CuestionarioService.generar(semilla, 10);
            assertEquals(10, preguntas.size());
            assertEquals(10, new HashSet<>(preguntas.stream().map(CuestionarioService.Pregunta::enunciado).toList()).size());
            for (var p : preguntas) {
                assertTrue(p.opciones().size() >= 2 && p.opciones().size() <= 4, p.enunciado());
                assertEquals(p.opciones().size(), new HashSet<>(p.opciones()).size(), p.enunciado());
                assertTrue(p.correcta() >= 0 && p.correcta() < p.opciones().size());
                assertFalse(p.explicacion().isBlank());
            }
        }
    }

    @Test void laOpcionCorrectaCoincideConLaFicha() {
        for (long semilla = 0; semilla < 100; semilla++) {
            for (var p : CuestionarioService.generar(semilla, 12)) {
                for (FichaAlgoritmo f : FichaAlgoritmo.todas()) {
                    String nombre = f.algoritmo().nombre();
                    String correcta = p.opciones().get(p.correcta());
                    if (p.enunciado().equals("¿Cuál es el peor caso de " + nombre + "?")) assertEquals(f.peor(), correcta);
                    if (p.enunciado().equals("¿Cuál es el mejor caso de " + nombre + "?")) assertEquals(f.mejor(), correcta);
                    if (p.enunciado().equals("¿Cuál es la memoria extra de " + nombre + "?")) assertEquals(f.espacio(), correcta);
                    if (p.enunciado().equals("¿" + nombre + " es un ordenamiento estable?")) assertEquals(f.estable() ? "Sí" : "No", correcta);
                    if (p.enunciado().equals("¿Qué técnica usa " + nombre + "?")) assertEquals(f.tecnica(), correcta);
                }
                if (p.enunciado().startsWith("¿Cuál de estos algoritmos NO ordena")) {
                    assertTrue(List.of("Counting Sort", "Radix Sort").contains(p.opciones().get(p.correcta())));
                }
            }
        }
    }

    @Test void hanoiUsaLaFormulaCorrecta() {
        for (long semilla = 0; semilla < 200; semilla++) {
            for (var p : CuestionarioService.generar(semilla, 12)) {
                if (p.enunciado().contains("movimientos como mínimo")) {
                    int n = Integer.parseInt(p.enunciado().replaceAll("\\D+(\\d+) discos.*", "$1"));
                    assertEquals(Integer.toString((1 << n) - 1), p.opciones().get(p.correcta()));
                }
            }
        }
    }

    // ---------- Logros ----------

    private static EstadisticasDAO.IntentoReciente intento(Boolean correcto) {
        return new EstadisticasDAO.IntentoReciente(1L, LocalDateTime.now(), "Quick Sort", "PRACTICA", correcto, 5);
    }

    @Test void rachaMaximaIgnoraLosNoCalificados() {
        assertEquals(3, LogrosService.rachaMaxima(List.of(intento(true), intento(null), intento(true), intento(true),
                intento(false), intento(true))));
        assertEquals(0, LogrosService.rachaMaxima(List.of()));
    }

    @Test void logrosSeDesbloqueanConLosUmbrales() {
        var vacio = LogrosService.evaluar(new LogrosService.Datos(0, 0, 0, 0, 0, 0, 0));
        assertTrue(vacio.stream().noneMatch(LogrosService.Logro::desbloqueado));
        var lleno = LogrosService.evaluar(new LogrosService.Datos(60, 55, 5, 12, 6, 250, 10));
        assertTrue(lleno.stream().allMatch(LogrosService.Logro::desbloqueado), lleno.toString());
        var parcial = LogrosService.evaluar(new LogrosService.Datos(12, 6, 6, 5, 0, 0, 0));
        assertTrue(parcial.stream().filter(l -> l.titulo().equals("Constante")).findFirst().orElseThrow().desbloqueado());
        assertFalse(parcial.stream().filter(l -> l.titulo().equals("Francotirador")).findFirst().orElseThrow().desbloqueado());
        assertTrue(parcial.stream().filter(l -> l.titulo().equals("En racha")).findFirst().orElseThrow().desbloqueado());
    }

    @Test void algoritmosDominadosExigenCincoIntentosYOchentaPorCiento() {
        var rendimiento = List.of(
                new EstadisticasDAO.RendimientoAlgoritmo("Quick Sort", 5, 4, 1),
                new EstadisticasDAO.RendimientoAlgoritmo("Merge Sort", 5, 3, 2),
                new EstadisticasDAO.RendimientoAlgoritmo("Heap Sort", 3, 3, 0));
        assertEquals(1, LogrosService.algoritmosDominados(rendimiento));
    }
}
