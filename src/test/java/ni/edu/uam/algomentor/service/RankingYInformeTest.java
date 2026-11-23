package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.model.Perfil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingYInformeTest {

    @TempDir
    Path directorio;

    private static EstadisticasDAO.ResumenPerfil perfil(long id, String nombre, long total, long ok, long mal) {
        return new EstadisticasDAO.ResumenPerfil(id, nombre, new EstadisticasDAO.ResumenIntentos(total, ok, mal));
    }

    @Test
    void elRankingOrdenaPorAciertosLuegoPorPorcentajeYDejaAlFinalALosSinIntentos() {
        var ranking = ResumenAdministracionService.clasificar(List.of(
                perfil(1, "ana", 0, 0, 0),
                perfil(2, "luis", 10, 5, 5),
                perfil(3, "marta", 6, 5, 1),
                perfil(4, "beto", 5, 5, 0)));

        assertEquals(List.of("beto", "marta", "luis", "ana"),
                ranking.stream().map(ResumenAdministracionService.PuestoRanking::nombre).toList());
        assertEquals(1, ranking.getFirst().puesto());
        assertEquals(4, ranking.getLast().puesto());
        assertEquals(0.0, ranking.getLast().porcentaje());
        assertEquals(50.0, ranking.get(2).porcentaje());
    }

    @Test
    void lasFilasDelInformeIncluyenSiempreLasCuatroSecciones() {
        List<Map<String, ?>> filas = FilasInforme.construir(List.of(), List.of(), List.of(), List.of());

        assertEquals(List.of("1", "2", "3", "4"), filas.stream().map(f -> (String) f.get("seccion")).toList());
        assertEquals("Sin intentos registrados.", filas.getLast().get("c1"));
    }

    @Test
    void lasFilasDelInformeResumenAlgoritmosErroresEHistorial() {
        var filas = FilasInforme.construir(
                List.of(new EstadisticasDAO.EstadisticaHabilidad(1L, "Signos", 1, 3)),
                List.of(new EstadisticasDAO.RendimientoAlgoritmo("Gauss", 4, 1, 3)),
                List.of(new EstadisticasDAO.ErrorPorTipo("SIGNO", 3)),
                List.of(new EstadisticasDAO.IntentoReciente(1L, LocalDateTime.of(2026, 11, 22, 18, 9),
                        "Gauss", "PRACTICA", false, 42)));

        assertEquals(4, filas.size());
        assertEquals("25.0%", filas.get(0).get("c5"));
        assertEquals("25.0%", filas.get(1).get("c5"));
        assertEquals("Signo", filas.get(2).get("c1"));
        assertEquals("100.0%", filas.get(2).get("c3"));
        assertEquals("Incorrecto", filas.get(3).get("c4"));
        assertEquals("42 s", filas.get(3).get("c5"));
    }

    @Test
    void elHistorialDelInformeSeLimita() {
        List<EstadisticasDAO.IntentoReciente> muchos = java.util.stream.IntStream.range(0, 40)
                .mapToObj(i -> new EstadisticasDAO.IntentoReciente((long) i, LocalDateTime.of(2026, 1, 1, 0, 0),
                        "Gauss", "PRACTICA", true, 1))
                .toList();

        var filas = FilasInforme.construir(List.of(), List.of(), List.of(), muchos);

        assertEquals(FilasInforme.INTENTOS_EN_INFORME,
                filas.stream().filter(f -> "4".equals(f.get("seccion"))).count());
    }

    @Test
    void soloUnAdministradorPuedeGenerarElInformeDeOtroPerfil() throws Exception {
        Perfil perfil = new Perfil();
        perfil.setId(7L);
        perfil.setNombre("Otro");
        ReporteProgresoService.ProveedorEstadisticas vacio = new ReporteProgresoService.ProveedorEstadisticas() {
            @Override
            public EstadisticasDAO.ResumenIntentos obtenerResumenIntentos(Long perfilId) {
                return new EstadisticasDAO.ResumenIntentos(0, 0, 0);
            }

            @Override
            public List<EstadisticasDAO.EstadisticaHabilidad> listarProgresoPorHabilidad(Long perfilId) {
                return List.of();
            }
        };
        var normal = new ReporteProgresoService(id -> Optional.of(perfil), vacio,
                new AutorizacionService(id -> false));
        var admin = new ReporteProgresoService(id -> Optional.of(perfil), vacio,
                new AutorizacionService(id -> id == 1L));

        assertThrows(SecurityException.class,
                () -> normal.generarReporte(2L, 7L, directorio.resolve("a.pdf")));
        assertTrue(java.nio.file.Files.notExists(directorio.resolve("a.pdf")));
        assertTrue(java.nio.file.Files.exists(admin.generarReporte(1L, 7L, directorio.resolve("b.pdf"))));
        assertTrue(java.nio.file.Files.exists(normal.generarReporte(7L, 7L, directorio.resolve("c.pdf"))));
    }
}
