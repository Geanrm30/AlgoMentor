package ni.edu.uam.algomentor.report;

import net.sf.jasperreports.engine.JasperCompileManager;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReporteProgresoTemplateTest {

    @Test
    void compilaLaPlantillaDeProgreso() throws Exception {
        try (InputStream plantilla = getClass().getResourceAsStream(
                "/ni/edu/uam/algomentor/report/progreso.jrxml")) {
            assertNotNull(plantilla, "No se encontró la plantilla de progreso");
            JasperCompileManager.compileReport(plantilla);
        }
    }
}
