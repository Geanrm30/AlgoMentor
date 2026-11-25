package ni.edu.uam.algomentor.report;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRPrintRectangle;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReporteDificultadRealTemplateTest {

    @Test
    void compilaLaPlantillaDeDificultadReal() throws Exception {
        try (var plantilla = getClass().getResourceAsStream(
                "/ni/edu/uam/algomentor/report/dificultad-real.jrxml")) {
            assertNotNull(plantilla, "No se encontró la plantilla de dificultad real");
            assertNotNull(JasperCompileManager.compileReport(plantilla));
        }
    }

    @Test
    void losCriteriosLargosSeMuestranCompletosSinSolaparseConLaExplicacionNiLaTabla() throws Exception {
        String criterios = ("Rangos inclusivos por nivel y mínimo de intentos evaluados. "
                + "Esta explicación extensa comprueba que los criterios conservan todas sus líneas. ").repeat(6)
                + "FIN DE LOS CRITERIOS.";
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("solicitante", "Demostración");
        parametros.put("fechaGeneracion", "01/01/2026 12:00");
        parametros.put("porcentajeGlobal", "—");
        parametros.put("criterios", criterios);
        for (String nombre : List.of("totalEjercicios", "bienCalibrados", "malCalibrados", "datosInsuficientes",
                "totalIntentos", "totalAciertos", "totalErrores")) {
            parametros.put(nombre, 0L);
        }
        try (var entrada = getClass().getResourceAsStream("/ni/edu/uam/algomentor/report/dificultad-real.jrxml")) {
            assertNotNull(entrada);
            var plantilla = JasperCompileManager.compileReport(entrada);
            var reporte = JasperFillManager.fillReport(plantilla, parametros, new JREmptyDataSource(0));
            assertEquals(1, reporte.getPages().size());
            var elementos = reporte.getPages().getFirst().getElements();
            var textos = elementos.stream().filter(JRPrintText.class::isInstance).map(JRPrintText.class::cast).toList();
            JRPrintText rangos = textos.stream().filter(t -> t.getFullText().startsWith("Rangos inclusivos"))
                    .findFirst().orElseThrow();
            JRPrintText explicacion = textos.stream().filter(t -> t.getFullText().startsWith("Reglas heurísticas"))
                    .findFirst().orElseThrow();
            JRPrintText cabecera = textos.stream().filter(t -> "ID".equals(t.getFullText()))
                    .findFirst().orElseThrow();
            JRPrintRectangle fondo = elementos.stream().filter(JRPrintRectangle.class::isInstance)
                    .map(JRPrintRectangle.class::cast).filter(r -> r.getWidth() == 515
                            && Color.decode("#F8FAFC").equals(r.getBackcolor())).findFirst().orElseThrow();

            assertEquals(criterios, rangos.getFullText());
            assertNull(rangos.getTextTruncateIndex());
            assertTrue(rangos.getY() + rangos.getHeight() <= explicacion.getY());
            assertTrue(explicacion.getFullText().endsWith("sin intentos evaluados."));
            assertNull(explicacion.getTextTruncateIndex());
            assertTrue(explicacion.getY() + explicacion.getHeight() <= fondo.getY() + fondo.getHeight());
            assertTrue(fondo.getY() + fondo.getHeight() <= cabecera.getY());
        }
    }
}
