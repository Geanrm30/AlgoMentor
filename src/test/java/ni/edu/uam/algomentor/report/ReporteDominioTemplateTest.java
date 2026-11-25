package ni.edu.uam.algomentor.report;

import net.sf.jasperreports.engine.JasperCompileManager;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReporteDominioTemplateTest {

    @Test
    void compilaLaPlantillaDeDominio() throws Exception {
        try (InputStream plantilla = getClass().getResourceAsStream(
                "/ni/edu/uam/algomentor/report/dominio.jrxml")) {
            assertNotNull(plantilla, "No se encontró la plantilla de dominio");
            JasperCompileManager.compileReport(plantilla);
        }
    }
}
