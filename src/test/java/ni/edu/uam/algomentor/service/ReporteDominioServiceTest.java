package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.model.Algoritmo;
import ni.edu.uam.algomentor.model.Perfil;
import net.sf.jasperreports.engine.JRPrintText;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReporteDominioServiceTest {

    @TempDir
    Path directorioTemporal;

    @Test
    void generaPdfDeConstanciaCuandoAlcanzaElUmbral() throws Exception {
        ReporteDominioService servicio = crearServicio(new EstadisticasDAO.DominioAlgoritmo(5, 4));

        Path generado = servicio.generarReporte(1L, 2L,
                directorioTemporal.resolve("dominio.pdf"));

        assertPdfValido(generado);
        var reporte = servicio.prepararReporte(1L, 2L);
        assertEquals(1, reporte.getPages().size());
        assertTrue(textoDelReporte(reporte).contains("CONSTANCIA DE DOMINIO"));
        assertTrue(textoDelReporte(reporte).contains("Domina Quick Sort"));
    }

    @Test
    void generaPdfConPlanCuandoNoAlcanzaElUmbral() throws Exception {
        ReporteDominioService servicio = crearServicio(new EstadisticasDAO.DominioAlgoritmo(5, 3));

        Path generado = servicio.generarReporte(1L, 2L,
                directorioTemporal.resolve("plan.pdf"));

        assertPdfValido(generado);
        var reporte = servicio.prepararReporte(1L, 2L);
        assertTrue(textoDelReporte(reporte).contains("PLAN PERSONALIZADO"));
        assertTrue(textoDelReporte(reporte).contains("Tu resultado actual es 60.0%"));
    }

    @Test
    void generaPlanCuandoNoHayIntentosCalificados() throws Exception {
        ReporteDominioService servicio = crearServicio(new EstadisticasDAO.DominioAlgoritmo(0, 0));

        var reporte = servicio.prepararReporte(1L, 2L);
        assertEquals(1, reporte.getPages().size());
        assertTrue(textoDelReporte(reporte).contains("Aún no tienes intentos evaluados"));
    }

    @Test
    void rechazaPerfilInexistente() {
        ReporteDominioService servicio = new ReporteDominioService(
                id -> Optional.empty(), id -> Optional.of(algoritmo()),
                (perfilId, algoritmoId) -> new EstadisticasDAO.DominioAlgoritmo(1, 1));

        assertThrows(IllegalArgumentException.class, () -> servicio.prepararReporte(1L, 2L));
    }

    @Test
    void rechazaGenerarConstanciaDeOtroPerfilSinPermiso() {
        ReporteDominioService servicio = crearServicio(new EstadisticasDAO.DominioAlgoritmo(5, 4));

        assertThrows(SecurityException.class,
                () -> servicio.prepararReporte(9L, 1L, 2L));
    }

    private ReporteDominioService crearServicio(EstadisticasDAO.DominioAlgoritmo dominio) {
        Perfil perfil = new Perfil();
        perfil.setId(1L);
        perfil.setNombre("Estudiante de prueba");
        return new ReporteDominioService(
                id -> Optional.of(perfil),
                id -> Optional.of(algoritmo()),
                (perfilId, algoritmoId) -> dominio
        );
    }

    private static Algoritmo algoritmo() {
        Algoritmo algoritmo = new Algoritmo();
        algoritmo.setId(2L);
        algoritmo.setNombre("Quick Sort");
        algoritmo.setDescripcion("Ordena elementos mediante particiones.");
        return algoritmo;
    }

    private static String textoDelReporte(net.sf.jasperreports.engine.JasperPrint reporte) {
        return reporte.getPages().get(0).getElements().stream()
                .filter(JRPrintText.class::isInstance)
                .map(elemento -> (JRPrintText) elemento)
                .map(JRPrintText::getFullText)
                .collect(Collectors.joining(" "));
    }

    private void assertPdfValido(Path archivo) throws Exception {
        byte[] bytes = Files.readAllBytes(archivo);
        assertTrue(bytes.length > 4);
        assertArrayEquals("%PDF".getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                java.util.Arrays.copyOf(bytes, 4));
    }
}
