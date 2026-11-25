package ni.edu.uam.algomentor.service;

import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperPrint;
import ni.edu.uam.algomentor.dao.MapaCalorErroresDAO.ErroresPerfil;
import ni.edu.uam.algomentor.model.Perfil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ReporteMapaCalorServiceTest {

    @TempDir
    Path directorio;

    private static ReporteMapaCalorService servicio(List<ErroresPerfil> datos) {
        return new ReporteMapaCalorService(() -> datos, id -> {
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
    void resumeTotalesIdentificaFocoCriticoYCalculaPorcentajes() throws Exception {
        var reporte = servicio(List.of(
                new ErroresPerfil(1L, "Ana", 6, 2, 1, 1),
                new ErroresPerfil(2L, "Bruno", 2, 4, 0, 4),
                new ErroresPerfil(3L, "Carlos", 0, 0, 0, 0))).prepararReporte(1L);

        String contenido = texto(reporte);
        assertEquals(1, reporte.getPages().size());
        assertTrue(contenido.contains("Mapa de calor de errores por perfil"));
        assertTrue(contenido.contains("Solicitado por admin"));
        assertTrue(contenido.contains("Signos: 8"));
        assertTrue(contenido.contains("Fracciones: 6"));
        assertTrue(contenido.contains("Aritmético: 1"));
        assertTrue(contenido.contains("Op. inválida: 5"));
        assertTrue(contenido.contains("Foco grupal principal: Signos"));
        assertTrue(contenido.contains("Ana (10)"));
        assertTrue(contenido.contains("50.0%"));
        assertTrue(contenido.contains("0.0%"));
    }

    @Test
    void calculaEscalaTermicaCorrectamente() {
        assertEquals(0, ReporteMapaCalorService.calcularNivel(0, 10));
        assertEquals(1, ReporteMapaCalorService.calcularNivel(1, 1));
        assertEquals(2, ReporteMapaCalorService.calcularNivel(2, 2));
        assertEquals(3, ReporteMapaCalorService.calcularNivel(3, 3));
        assertEquals(1, ReporteMapaCalorService.calcularNivel(3, 10));
        assertEquals(2, ReporteMapaCalorService.calcularNivel(6, 10));
        assertEquals(3, ReporteMapaCalorService.calcularNivel(9, 10));
    }

    @Test
    void exportaPdfValidoConContratoDeInformeAdministrativo() throws Exception {
        Path destino = directorio.resolve("mapa-calor.pdf");

        Path generado = servicio(List.of(new ErroresPerfil(1L, "Ana", 3, 1, 0, 2)))
                .guardarPdf(1L, destino);

        assertEquals(destino, generado);
        byte[] pdf = Files.readAllBytes(generado);
        assertTrue(pdf.length > 1000);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void deniegaAccesoAntesDeConsultarDatosOEscribirPdf() {
        AtomicInteger consultas = new AtomicInteger();
        var servicio = new ReporteMapaCalorService(() -> {
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
    void vuelveAComprobarRolAdministradorEnCadaGeneracion() throws Exception {
        AtomicBoolean administrador = new AtomicBoolean(true);
        AtomicInteger consultas = new AtomicInteger();
        var servicio = new ReporteMapaCalorService(() -> {
            consultas.incrementAndGet();
            return List.of();
        }, id -> Optional.empty(), new AutorizacionService(id -> administrador.get()));

        servicio.prepararReporte(1L);
        administrador.set(false);

        assertThrows(SecurityException.class, () -> servicio.prepararReporte(1L));
        assertEquals(1, consultas.get());
    }

    @Test
    void sinPerfilesMuestraResumenVacioSinDivisionPorCero() throws Exception {
        var reporte = servicio(List.of()).prepararReporte(1L);
        String contenido = texto(reporte);

        assertEquals(1, reporte.getPages().size());
        assertTrue(contenido.contains("Todavía no hay perfiles registrados."));
        assertTrue(contenido.contains("Ninguno"));
        assertFalse(contenido.contains("NaN"));
        assertFalse(contenido.contains("Infinity"));
    }

    @Test
    void paginaMuchosPerfilesYRepiteCabecera() throws Exception {
        String nombreLargo = "Estudiante con nombre largo para verificar ajuste en la tabla";
        var datos = IntStream.rangeClosed(1, 50)
                .mapToObj(i -> new ErroresPerfil((long) i, i == 50 ? nombreLargo : "Perfil " + i, 2, 1, 0, 1))
                .toList();

        var reporte = servicio(datos).prepararReporte(1L);

        assertTrue(reporte.getPages().size() > 1);
        for (var pagina : reporte.getPages()) {
            assertTrue(pagina.getElements().stream().filter(JRPrintText.class::isInstance)
                    .map(JRPrintText.class::cast).anyMatch(t -> "PERFIL".equals(t.getFullText())));
        }
        assertTrue(texto(reporte).contains(nombreLargo));
    }
}
