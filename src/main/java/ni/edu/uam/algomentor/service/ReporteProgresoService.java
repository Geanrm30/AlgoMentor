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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

public class ReporteProgresoService {

    private static final String PLANTILLA =
            "/ni/edu/uam/algomentor/report/progreso.jrxml";
    private static final int RECIENTES_CONSULTADOS = 100;
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"));

    @FunctionalInterface
    public interface ProveedorPerfil {
        Optional<Perfil> buscarPorId(Long perfilId);
    }

    public interface ProveedorEstadisticas {
        EstadisticasDAO.ResumenIntentos obtenerResumenIntentos(Long perfilId);
        List<EstadisticasDAO.EstadisticaHabilidad> listarProgresoPorHabilidad(Long perfilId);

        default List<EstadisticasDAO.RendimientoAlgoritmo> listarRendimientoPorAlgoritmo(Long perfilId) {
            return List.of();
        }

        default List<EstadisticasDAO.ErrorPorTipo> listarErroresPorTipo(Long perfilId) {
            return List.of();
        }

        default List<EstadisticasDAO.IntentoReciente> listarIntentosRecientes(Long perfilId, int limite) {
            return List.of();
        }

        default OptionalDouble obtenerDuracionPromedioSegundos(Long perfilId) {
            return OptionalDouble.empty();
        }
    }

    private final ProveedorPerfil perfilService;
    private final ProveedorEstadisticas estadisticasDAO;
    private final AutorizacionService autorizacion;

    public ReporteProgresoService() {
        this(
                new PerfilService()::buscarPorId,
                crearProveedorEstadisticas(new EstadisticasDAO()),
                new AutorizacionService()
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
        this(perfilService, estadisticasDAO, null);
    }

    /** Con {@code autorizacion} nulo, solo se permite pedir el informe del propio perfil. */
    ReporteProgresoService(
            ProveedorPerfil perfilService,
            ProveedorEstadisticas estadisticasDAO,
            AutorizacionService autorizacion
    ) {
        this.perfilService = Objects.requireNonNull(perfilService);
        this.estadisticasDAO = Objects.requireNonNull(estadisticasDAO);
        this.autorizacion = autorizacion;
    }

    /**
     * Genera el informe de {@code perfilId} en nombre de {@code solicitanteId}. Cada quien puede pedir el
     * suyo; el de otro perfil solo lo puede pedir un administrador.
     *
     * @throws SecurityException si el solicitante no tiene acceso a ese perfil.
     */
    public Path generarReporte(Long solicitanteId, Long perfilId, Path destino)
            throws JRException, IOException {
        exigirAcceso(solicitanteId, perfilId);
        return generarReporte(perfilId, destino);
    }

    public JasperPrint prepararReporte(Long solicitanteId, Long perfilId)
            throws JRException, IOException {
        exigirAcceso(solicitanteId, perfilId);
        return prepararReporte(perfilId);
    }

    private void exigirAcceso(Long solicitanteId, Long perfilId) {
        if (autorizacion == null) {
            if (solicitanteId == null || !solicitanteId.equals(perfilId)) {
                throw new SecurityException("No tienes acceso al informe de otro perfil.");
            }
            return;
        }
        autorizacion.exigirAccesoAPerfil(solicitanteId, perfilId);
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
        List<EstadisticasDAO.EstadisticaHabilidad> habilidades =
                estadisticasDAO.listarProgresoPorHabilidad(perfilId);
        List<EstadisticasDAO.RendimientoAlgoritmo> algoritmos =
                estadisticasDAO.listarRendimientoPorAlgoritmo(perfilId);
        List<EstadisticasDAO.ErrorPorTipo> errores = estadisticasDAO.listarErroresPorTipo(perfilId);
        List<EstadisticasDAO.IntentoReciente> recientes =
                estadisticasDAO.listarIntentosRecientes(perfilId, RECIENTES_CONSULTADOS);
        OptionalDouble duracion = estadisticasDAO.obtenerDuracionPromedioSegundos(perfilId);
        List<Map<String, ?>> filas = FilasInforme.construir(habilidades, algoritmos, errores, recientes);

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
        long calificados = resumen.correctos() + resumen.incorrectos();
        parametros.put("porcentajeAcierto", calificados == 0 ? "—"
                : FilasInforme.porcentaje(resumen.correctos() * 100.0 / calificados));
        parametros.put("rachaActual", (long) EstadisticasDAO.rachaActual(recientes));
        parametros.put("tiempoPromedio", duracion.isPresent()
                ? String.format(Locale.ROOT, "%.0f s", duracion.getAsDouble()) : "—");
        parametros.put("algoritmosUsados", (long) algoritmos.size());

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

            @Override
            public List<EstadisticasDAO.RendimientoAlgoritmo> listarRendimientoPorAlgoritmo(Long perfilId) {
                return dao.listarRendimientoPorAlgoritmo(perfilId);
            }

            @Override
            public List<EstadisticasDAO.ErrorPorTipo> listarErroresPorTipo(Long perfilId) {
                return dao.listarErroresPorTipo(perfilId);
            }

            @Override
            public List<EstadisticasDAO.IntentoReciente> listarIntentosRecientes(Long perfilId, int limite) {
                return dao.listarIntentosRecientes(perfilId, limite);
            }

            @Override
            public OptionalDouble obtenerDuracionPromedioSegundos(Long perfilId) {
                return dao.obtenerDuracionPromedioSegundos(perfilId);
            }
        };
    }
}
