package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.CatalogoDAO;
import ni.edu.uam.algomentor.model.Perfil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReporteCatalogoServiceTest {

    @TempDir
    Path directorio;

    private static ReporteCatalogoService.ProveedorCatalogo catalogo(
            List<CatalogoDAO.AlgoritmoCatalogo> algoritmos, AtomicInteger consultas) {
        return new ReporteCatalogoService.ProveedorCatalogo() {
            @Override
            public List<CatalogoDAO.AlgoritmoCatalogo> listarAlgoritmos() {
                consultas.incrementAndGet();
                return algoritmos;
            }

            @Override
            public List<CatalogoDAO.ConteoClave> listarEjerciciosPorDificultad() {
                consultas.incrementAndGet();
                return algoritmos.isEmpty() ? List.of()
                        : List.of(new CatalogoDAO.ConteoClave("1", 3), new CatalogoDAO.ConteoClave("3", 1));
            }

            @Override
            public List<CatalogoDAO.ConteoClave> listarEjerciciosPorOrigen() {
                consultas.incrementAndGet();
                return algoritmos.isEmpty() ? List.of()
                        : List.of(new CatalogoDAO.ConteoClave("MANUAL", 3), new CatalogoDAO.ConteoClave("ALEATORIO", 1));
            }

            @Override
            public List<CatalogoDAO.EjercicioUsado> listarEjerciciosMasUsados(int limite) {
                consultas.incrementAndGet();
                return algoritmos.isEmpty() ? List.of()
                        : List.of(new CatalogoDAO.EjercicioUsado(7L, "Bubble Sort", 2, "MANUAL", 5));
            }
        };
    }

    private static ReporteCatalogoService servicio(
            ReporteCatalogoService.ProveedorCatalogo catalogo, long administradorId) {
        return new ReporteCatalogoService(catalogo,
                id -> {
                    Perfil perfil = new Perfil();
                    perfil.setNombre("admin");
                    return Optional.of(perfil);
                },
                new AutorizacionService(id -> id != null && id == administradorId));
    }

    @Test
    void unAdministradorGeneraElPdfDelCatalogo() throws Exception {
        var algoritmos = List.of(
                new CatalogoDAO.AlgoritmoCatalogo("Bubble Sort", "ORDENAMIENTO", "O(n^2)", 3, 6, 4),
                new CatalogoDAO.AlgoritmoCatalogo("Gauss", "MATEMATICO", "O(n^3)", 1, 0, 0));
        Path destino = directorio.resolve("catalogo.pdf");

        Path generado = servicio(catalogo(algoritmos, new AtomicInteger()), 1L).generarReporte(1L, destino);

        assertEquals(destino, generado);
        byte[] pdf = Files.readAllBytes(generado);
        assertTrue(pdf.length > 1000);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void unUsuarioNormalNoPuedeGenerarElInformeNiSeConsultaElCatalogo() {
        AtomicInteger consultas = new AtomicInteger();
        var servicio = servicio(catalogo(List.of(), consultas), 1L);

        assertThrows(SecurityException.class,
                () -> servicio.generarReporte(2L, directorio.resolve("no.pdf")));
        assertThrows(SecurityException.class, () -> servicio.prepararReporte(null));
        assertEquals(0, consultas.get());
        assertTrue(Files.notExists(directorio.resolve("no.pdf")));
    }

    @Test
    void elInformeSeGeneraTambienConElCatalogoVacio() throws Exception {
        Path destino = directorio.resolve("vacio.pdf");

        servicio(catalogo(List.of(), new AtomicInteger()), 1L).generarReporte(1L, destino);

        assertTrue(Files.size(destino) > 1000);
    }

    @Test
    void lasFilasIncluyenSiempreLasCuatroSecciones() {
        List<Map<String, ?>> filas = FilasCatalogo.construir(List.of(), List.of(), List.of(), List.of());

        assertEquals(List.of("1", "2", "3", "4"), filas.stream().map(f -> (String) f.get("seccion")).toList());
        assertEquals("Sin ejercicios registrados.", filas.getLast().get("c1"));
    }

    @Test
    void lasFilasResumenAlgoritmosDificultadOrigenYEjerciciosUsados() {
        var filas = FilasCatalogo.construir(
                List.of(new CatalogoDAO.AlgoritmoCatalogo("Gauss", "MATEMATICO", "O(n^3)", 2, 4, 1)),
                List.of(new CatalogoDAO.ConteoClave("1", 3), new CatalogoDAO.ConteoClave("2", 1)),
                List.of(new CatalogoDAO.ConteoClave("MANUAL", 4)),
                List.of(new CatalogoDAO.EjercicioUsado(9L, "Gauss", 2, "MANUAL", 4)));

        assertEquals(5, filas.size());
        assertEquals("25.0%", filas.getFirst().get("c5"));
        assertEquals("Nivel 1", filas.get(1).get("c1"));
        assertEquals("75.0%", filas.get(1).get("c3"));
        assertEquals("Manual", filas.get(3).get("c1"));
        assertEquals("#9", filas.getLast().get("c5"));
    }
}
