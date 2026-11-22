package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.model.Perfil;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.JasperExportManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ReporteProgresoService {

    private static final String PLANTILLA =
            "/ni/edu/uam/algomentor/report/progreso.jrxml";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"));

    @FunctionalInterface
    public interface ProveedorPerfil {
        Optional<Perfil> buscarPorId(Long perfilId);
    }

    public interface ProveedorEstadisticas {
        EstadisticasDAO.ResumenIntentos obtenerResumenIntentos(Long perfilId);
        List<EstadisticasDAO.EstadisticaHabilidad> listarProgresoPorHabilidad(Long perfilId);
    }

    private final ProveedorPerfil perfilService;
    private final ProveedorEstadisticas estadisticasDAO;

    public ReporteProgresoService() {
        this(
                new PerfilService()::buscarPorId,
                crearProveedorEstadisticas(new EstadisticasDAO())
        );
    }

    public ReporteProgresoService(
            PerfilService perfilService,
            EstadisticasDAO estadisticasDAO
    ) {
        this(
                Objects.requireNonNull(perfilService)::buscarPorId,
                crearProveedorEstadisticas(Objects.requireNonNull(estadisticasDAO))
        );
    }

    public ReporteProgresoService(
            ProveedorPerfil perfilService,
            ProveedorEstadisticas estadisticasDAO
    ) {
        this.perfilService = Objects.requireNonNull(perfilService);
        this.estadisticasDAO = Objects.requireNonNull(estadisticasDAO);
    }

    public Path generarReporte(Long perfilId, Path destino)
            throws JRException, IOException {
        Objects.requireNonNull(destino, "El destino del reporte es obligatorio.");

        JasperPrint reporte = prepararReporte(perfilId);
        byte[] pdf = JasperExportManager.exportReportToPdf(reporte);
        return Files.write(destino, pdf);
    }

    public JasperPrint prepararReporte(Long perfilId) throws JRException, IOException {
        EstadisticasDAO.validarPerfilId(perfilId);
        Perfil perfil = perfilService.buscarPorId(perfilId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un perfil con el ID indicado."
                ));
        EstadisticasDAO.ResumenIntentos resumen =
                estadisticasDAO.obtenerResumenIntentos(perfilId);
        List<Map<String, ?>> filas = new ArrayList<>();
        estadisticasDAO.listarProgresoPorHabilidad(perfilId)
                .forEach(estadistica -> {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("nombre", estadistica.nombre());
                    fila.put("aciertos", estadistica.aciertos());
                    fila.put("errores", estadistica.errores());
                    fila.put("porcentajeAciertos", estadistica.porcentajeAciertos());
                    filas.add(fila);
                });

        JasperReport plantillaCompilada;
        try (InputStream plantilla = ReporteProgresoService.class
                .getResourceAsStream(PLANTILLA)) {
            if (plantilla == null) {
                throw new IOException("No se encontró la plantilla de progreso: " + PLANTILLA);
            }
            plantillaCompilada = JasperCompileManager.compileReport(plantilla);
        }

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("perfilNombre", perfil.getNombre());
        parametros.put("fechaGeneracion", LocalDateTime.now().format(FORMATO_FECHA));
        parametros.put("totalIntentos", resumen.total());
        parametros.put("intentosCorrectos", resumen.correctos());
        parametros.put("intentosIncorrectos", resumen.incorrectos());
        parametros.put("intentosSinCalificar", resumen.sinCalificar());

        return JasperFillManager.fillReport(
                plantillaCompilada,
                parametros,
                new JRMapCollectionDataSource(filas)
        );
    }

    private static ProveedorEstadisticas crearProveedorEstadisticas(
            EstadisticasDAO dao
    ) {
        return new ProveedorEstadisticas() {
            @Override
            public EstadisticasDAO.ResumenIntentos obtenerResumenIntentos(Long perfilId) {
                return dao.obtenerResumenIntentos(perfilId);
            }

            @Override
            public List<EstadisticasDAO.EstadisticaHabilidad> listarProgresoPorHabilidad(
                    Long perfilId
            ) {
                return dao.listarProgresoPorHabilidad(perfilId);
            }
        };
    }
}
