package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.model.Perfil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReporteProgresoServiceTest {

    @TempDir
    Path directorioTemporal;

    @Test
    void generaPdfConProgresoPorHabilidad() throws Exception {
        ReporteProgresoService servicio = crearServicio(List.of(
                new EstadisticasDAO.EstadisticaHabilidad(
                        1L, "Fracciones", 3, 1
                )
        ));
        Path destino = directorioTemporal.resolve("progreso.pdf");

        Path generado = servicio.generarReporte(1L, destino);

        assertEquals(destino, generado);
        assertPdfValido(generado);
    }

    @Test
    void generaPdfAunqueElPerfilNoTengaProgresoPorHabilidad() throws Exception {
        ReporteProgresoService servicio = crearServicio(List.of());
        Path destino = directorioTemporal.resolve("sin-progreso.pdf");

        Path generado = servicio.generarReporte(1L, destino);

        assertEquals(destino, generado);
        assertPdfValido(generado);
    }

    @Test
    void rechazaUnPerfilInexistenteSinCrearElArchivo() {
        ReporteProgresoService servicio = new ReporteProgresoService(
                perfilId -> Optional.empty(),
                proveedorEstadisticas(List.of())
        );
        Path destino = directorioTemporal.resolve("inexistente.pdf");

        assertThrows(IllegalArgumentException.class,
                () -> servicio.generarReporte(1L, destino));
        assertTrue(Files.notExists(destino));
    }

    @Test
    void rechazaUnIdentificadorDePerfilInvalido() {
        ReporteProgresoService servicio = crearServicio(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> servicio.generarReporte(0L,
                        directorioTemporal.resolve("invalido.pdf")));
    }

    private ReporteProgresoService crearServicio(
            List<EstadisticasDAO.EstadisticaHabilidad> habilidades
    ) {
        Perfil perfil = new Perfil();
        perfil.setId(1L);
        perfil.setNombre("Estudiante de prueba");
        return new ReporteProgresoService(
                perfilId -> Optional.of(perfil),
                proveedorEstadisticas(habilidades)
        );
    }

    private ReporteProgresoService.ProveedorEstadisticas proveedorEstadisticas(
            List<EstadisticasDAO.EstadisticaHabilidad> habilidades
    ) {
        return new ReporteProgresoService.ProveedorEstadisticas() {
            @Override
            public EstadisticasDAO.ResumenIntentos obtenerResumenIntentos(Long perfilId) {
                return new EstadisticasDAO.ResumenIntentos(4, 3, 1);
            }

            @Override
            public List<EstadisticasDAO.EstadisticaHabilidad> listarProgresoPorHabilidad(
                    Long perfilId
            ) {
                return habilidades;
            }
        };
    }

    private void assertPdfValido(Path archivo) throws IOException {
        byte[] bytes = Files.readAllBytes(archivo);
        assertTrue(bytes.length > 4);
        assertArrayEquals("%PDF".getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                java.util.Arrays.copyOf(bytes, 4));
    }
}
