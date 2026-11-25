package ni.edu.uam.algomentor.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroInformesTest {

    @Test
    void elPanelIncluyeDificultadRealSinReemplazarElCatalogo() {
        var informe = RegistroInformes.todos().stream().filter(i -> "dificultad-real".equals(i.id()))
                .findFirst().orElseThrow();

        assertTrue(informe instanceof ReporteDificultadRealService);
        assertEquals("Dificultad real de los ejercicios", informe.titulo());
        assertFalse(informe.secciones().isEmpty());
        assertEquals("catalogo", RegistroInformes.todos().getFirst().id());
    }

    @Test
    void elPanelIncluyeElInformeDelCatalogo() {
        List<InformeAdministrativo> informes = RegistroInformes.todos();

        assertFalse(informes.isEmpty());
        assertEquals("catalogo", informes.getFirst().id());
        assertFalse(informes.getFirst().secciones().isEmpty());
    }

    @Test
    void cadaInformeTieneDatosParaMostrarseYNombreDeArchivoPdf() {
        List<InformeAdministrativo> informes = RegistroInformes.todos();

        for (InformeAdministrativo informe : informes) {
            assertFalse(informe.titulo().isBlank());
            assertFalse(informe.descripcion().isBlank());
            assertTrue(informe.nombreArchivoSugerido().endsWith(".pdf"));
        }
    }

    @Test
    void losIdentificadoresYLosNombresDeArchivoNoSeRepiten() {
        List<InformeAdministrativo> informes = RegistroInformes.todos();

        assertEquals(informes.size(), new HashSet<>(informes.stream().map(InformeAdministrativo::id).toList()).size());
        assertEquals(informes.size(),
                new HashSet<>(informes.stream().map(InformeAdministrativo::nombreArchivoSugerido).toList()).size());
    }
}
