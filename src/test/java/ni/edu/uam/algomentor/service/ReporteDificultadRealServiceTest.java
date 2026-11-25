package ni.edu.uam.algomentor.service;

import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperPrint;
import ni.edu.uam.algomentor.dao.DificultadRealDAO.EstadisticaEjercicio;
import ni.edu.uam.algomentor.model.Perfil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ReporteDificultadRealServiceTest {

    @TempDir
    Path directorio;

    private static ReporteDificultadRealService servicio(List<EstadisticaEjercicio> datos) {
        return new ReporteDificultadRealService(() -> datos, id -> {
            Perfil perfil = new Perfil();
            perfil.setNombre("admin");
            return Optional.of(perfil);
        }, new AutorizacionService(id -> id == 1L));
    }

    private static List<JRPrintText> textos(JasperPrint reporte) {
        return reporte.getPages().stream().flatMap(p -> p.getElements().stream())
                .filter(JRPrintText.class::isInstance).map(JRPrintText.class::cast).toList();
    }

    private static String texto(JasperPrint reporte) {
        return String.join("\n", textos(reporte).stream().map(JRPrintText::getFullText).toList());
    }

    @Test
    void resumeLosDiagnosticosYCalculaElAciertoGlobalPonderado() throws Exception {
        var reporte = servicio(List.of(
                new EstadisticaEjercicio(1L, "Bubble Sort", 1, 10, 7, 3),
                new EstadisticaEjercicio(2L, "Gauss", 5, 20, 20, 0),
                new EstadisticaEjercicio(3L, "Merge Sort", 3, 0, 0, 0),
                new EstadisticaEjercicio(4L, "Quick Sort", 1, 1, 1, 0))).prepararReporte(1L);

        String contenido = texto(reporte);
        assertEquals(1, reporte.getPages().size());
        assertTrue(contenido.contains("Dificultad real de los ejercicios"));
        assertTrue(contenido.contains("Solicitado por admin"));
        assertTrue(contenido.contains("Intentos evaluados: 31"));
        assertTrue(contenido.contains("Aciertos: 28"));
        assertTrue(contenido.contains("Errores: 3"));
        assertTrue(contenido.contains("Acierto global: 90.3%"));
        assertTrue(contenido.contains("70.0%"));
        assertTrue(contenido.contains("100.0%"));
        assertTrue(contenido.contains("—"));
        assertEquals(1, textos(reporte).stream().filter(t -> "BIEN CALIBRADO".equals(t.getFullText())).count());
        assertEquals(1, textos(reporte).stream().filter(t -> "MAL CALIBRADO".equals(t.getFullText())).count());
        assertEquals(2, textos(reporte).stream().filter(t -> "DATOS INSUFICIENTES".equals(t.getFullText())
                && t.getY() > 300).count());
        JRPrintText mal = textos(reporte).stream().filter(t -> "MAL CALIBRADO".equals(t.getFullText()))
                .findFirst().orElseThrow();
        assertEquals(Color.decode("#B91C1C"), mal.getForecolor());
        assertTrue(mal.isBold());
    }

    @Test
    void exportaUnPdfConElMismoContratoDelPanel() throws Exception {
        Path destino = directorio.resolve("dificultad-real.pdf");

        Path generado = servicio(List.of(new EstadisticaEjercicio(1L, "Gauss", 3, 10, 5, 5)))
                .guardarPdf(1L, destino);

        assertEquals(destino, generado);
        byte[] pdf = Files.readAllBytes(generado);
        assertTrue(pdf.length > 1000);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void deniegaElAccesoAntesDeConsultarDatosOPerfilesOEscribirArchivos() {
        AtomicInteger consultas = new AtomicInteger();
        var servicio = new ReporteDificultadRealService(() -> {
            consultas.incrementAndGet();
            return List.of();
        }, id -> {
            consultas.incrementAndGet();
            return Optional.empty();
        }, new AutorizacionService(id -> id == 1L));
        Path destino = directorio.resolve("prohibido.pdf");

        assertThrows(SecurityException.class, () -> servicio.prepararReporte(2L));
        assertThrows(SecurityException.class, () -> servicio.prepararReporte(null));
        assertThrows(SecurityException.class, () -> servicio.guardarPdf(2L, destino));
        assertEquals(0, consultas.get());
        assertTrue(Files.notExists(destino));
    }

    @Test
    void vuelveAComprobarElRolEnCadaGeneracion() throws Exception {
        AtomicBoolean administrador = new AtomicBoolean(true);
        AtomicInteger consultas = new AtomicInteger();
        var servicio = new ReporteDificultadRealService(() -> {
            consultas.incrementAndGet();
            return List.of();
        }, id -> Optional.empty(), new AutorizacionService(id -> administrador.get()));

        servicio.prepararReporte(1L);
        administrador.set(false);

        assertThrows(SecurityException.class, () -> servicio.prepararReporte(1L));
        assertEquals(1, consultas.get());
    }

    @Test
    void sinEjerciciosMuestraResumenVacioYExplicaLaAusenciaDeDatos() throws Exception {
        var reporte = servicio(List.of()).prepararReporte(1L);
        String contenido = texto(reporte);

        assertEquals(1, reporte.getPages().size());
        assertTrue(contenido.contains("Todavía no hay ejercicios registrados."));
        assertTrue(contenido.contains("Intentos evaluados: 0"));
        assertTrue(contenido.contains("Acierto global: —"));
        assertTrue(contenido.contains("no criterios científicos"));
        assertFalse(contenido.contains("NaN"));
        assertFalse(contenido.contains("Infinity"));
    }

    @Test
    void losEjerciciosSinPracticaNoSeConfundenConCeroPorCientoDeAcierto() throws Exception {
        var reporte = servicio(List.of(
                new EstadisticaEjercicio(1L, "Gauss", 5, 0, 0, 0),
                new EstadisticaEjercicio(2L, "Gauss", 5, 10, 0, 10))).prepararReporte(1L);

        String contenido = texto(reporte);
        assertTrue(contenido.contains("DATOS INSUFICIENTES"));
        assertTrue(contenido.contains("BIEN CALIBRADO"));
        assertTrue(contenido.contains("0.0%"));
        assertTrue(textos(reporte).stream().anyMatch(t -> "—".equals(t.getFullText())));
    }

    @Test
    void paginaTodosLosEjerciciosYRepiteLasCabeceras() throws Exception {
        String nombreLargo = "Algoritmo de prueba con nombre largo para comprobar el ajuste de la fila completa";
        var datos = IntStream.rangeClosed(1, 80).mapToObj(i -> new EstadisticaEjercicio(
                (long) i, i == 80 ? nombreLargo : "Gauss", 3, 10, 5, 5)).toList();

        var reporte = servicio(datos).prepararReporte(1L);

        assertTrue(reporte.getPages().size() > 1);
        for (var pagina : reporte.getPages()) {
            assertTrue(pagina.getElements().stream().filter(JRPrintText.class::isInstance)
                    .map(JRPrintText.class::cast).anyMatch(t -> "ALGORITMO".equals(t.getFullText())));
        }
        assertTrue(texto(reporte).contains(nombreLargo));
        assertEquals(80, textos(reporte).stream().filter(t -> "BIEN CALIBRADO".equals(t.getFullText())).count());
    }
}
