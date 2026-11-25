package ni.edu.uam.algomentor.report;

import net.sf.jasperreports.engine.JRPrintRectangle;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReporteMapaCalorTemplateTest {

    @Test
    void compilaLaPlantillaDeMapaDeCalor() throws Exception {
        try (var plantilla = getClass().getResourceAsStream(
                "/ni/edu/uam/algomentor/report/mapa-calor.jrxml")) {
            assertNotNull(plantilla, "No se encontró la plantilla del mapa de calor");
            assertNotNull(JasperCompileManager.compileReport(plantilla));
        }
    }

    @Test
    void dibujaCeldasTermicasPorNivelYSinSolapamiento() throws Exception {
        String recomendacionLarga = ("Foco grupal principal detectado en Signos y Fracciones. "
                + "Esta recomendación extensa valida el crecimiento automático de la caja. ").repeat(4)
                + "FIN.";
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("solicitante", "Demostración");
        parametros.put("fechaGeneracion", "25/11/2026 08:00");
        parametros.put("totalPerfiles", 1L);
        parametros.put("totalErrores", 12L);
        parametros.put("perfilesConErrores", 1L);
        parametros.put("perfilesSinErrores", 0L);
        parametros.put("totalSigno", 0L);
        parametros.put("totalFraccion", 2L);
        parametros.put("totalAritmetico", 4L);
        parametros.put("totalOperacionInvalida", 6L);
        parametros.put("errorPredominante", "Op. inválida");
        parametros.put("perfilCritico", "Ana (12)");
        parametros.put("recomendacionDocente", recomendacionLarga);

        List<Map<String, ?>> filas = List.of(Map.ofEntries(
                Map.entry("perfil", "Ana"),
                Map.entry("signo", 0L),
                Map.entry("nivelSigno", 0),
                Map.entry("fraccion", 2L),
                Map.entry("nivelFraccion", 1),
                Map.entry("aritmetico", 4L),
                Map.entry("nivelAritmetico", 2),
                Map.entry("operacionInvalida", 6L),
                Map.entry("nivelOperacionInvalida", 3),
                Map.entry("total", 12L),
                Map.entry("pesoGrupo", "100.0%")
        ));

        try (var entrada = getClass().getResourceAsStream("/ni/edu/uam/algomentor/report/mapa-calor.jrxml")) {
            assertNotNull(entrada);
            var plantilla = JasperCompileManager.compileReport(entrada);
            var reporte = JasperFillManager.fillReport(plantilla, parametros, new JRMapCollectionDataSource(filas));
            assertEquals(1, reporte.getPages().size());
            var elementos = reporte.getPages().getFirst().getElements();
            var rectangulos = elementos.stream().filter(JRPrintRectangle.class::isInstance)
                    .map(JRPrintRectangle.class::cast).toList();
            var textos = elementos.stream().filter(JRPrintText.class::isInstance)
                    .map(JRPrintText.class::cast).toList();

            JRPrintText recText = textos.stream().filter(t -> t.getFullText().startsWith("Foco grupal"))
                    .findFirst().orElseThrow();
            JRPrintText cabecera = textos.stream().filter(t -> "PERFIL".equals(t.getFullText()))
                    .findFirst().orElseThrow();

            assertEquals(recomendacionLarga, recText.getFullText());
            assertNull(recText.getTextTruncateIndex());
            assertTrue(recText.getY() + recText.getHeight() <= cabecera.getY());

            assertTrue(rectangulos.stream().anyMatch(r -> r.getWidth() == 64 && Color.decode("#ECFDF5").equals(r.getBackcolor())));
            assertTrue(rectangulos.stream().anyMatch(r -> r.getWidth() == 68 && Color.decode("#FEF08A").equals(r.getBackcolor())));
            assertTrue(rectangulos.stream().anyMatch(r -> r.getWidth() == 68 && Color.decode("#FB923C").equals(r.getBackcolor())));
            assertTrue(rectangulos.stream().anyMatch(r -> r.getWidth() == 68 && Color.decode("#DC2626").equals(r.getBackcolor())));
        }
    }
}
