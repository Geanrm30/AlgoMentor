package ni.edu.uam.algomentor.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import ni.edu.uam.algomentor.dao.AlgoritmoDAO;
import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.model.Algoritmo;
import ni.edu.uam.algomentor.model.Perfil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ReporteDominioService {

    public static final double UMBRAL_DOMINIO = 80.0;
    private static final String PLANTILLA =
            "/ni/edu/uam/algomentor/report/dominio.jrxml";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"));

    @FunctionalInterface
    public interface ProveedorPerfil {
        Optional<Perfil> buscarPorId(Long perfilId);
    }

    @FunctionalInterface
    public interface ProveedorAlgoritmo {
        Optional<Algoritmo> buscarPorId(Long algoritmoId);
    }

    @FunctionalInterface
    public interface ProveedorEstadisticas {
        EstadisticasDAO.DominioAlgoritmo obtenerDominio(Long perfilId, Long algoritmoId);
    }

    private final ProveedorPerfil perfiles;
    private final ProveedorAlgoritmo algoritmos;
    private final ProveedorEstadisticas estadisticas;
    private final AutorizacionService autorizacion;

    public ReporteDominioService() {
        this(
                new PerfilService()::buscarPorId,
                id -> Optional.ofNullable(new AlgoritmoDAO().buscarPorId(id)),
                new EstadisticasDAO()::obtenerDominioAlgoritmo,
                new AutorizacionService()
        );
    }

    public ReporteDominioService(ProveedorPerfil perfiles, ProveedorAlgoritmo algoritmos,
                                 ProveedorEstadisticas estadisticas) {
        this(perfiles, algoritmos, estadisticas, null);
    }

    ReporteDominioService(ProveedorPerfil perfiles, ProveedorAlgoritmo algoritmos,
                          ProveedorEstadisticas estadisticas, AutorizacionService autorizacion) {
        this.perfiles = Objects.requireNonNull(perfiles);
        this.algoritmos = Objects.requireNonNull(algoritmos);
        this.estadisticas = Objects.requireNonNull(estadisticas);
        this.autorizacion = autorizacion;
    }

    public Path generarReporte(Long perfilId, Long algoritmoId, Path destino)
            throws JRException, IOException {
        return generarReporte(perfilId, perfilId, algoritmoId, destino);
    }

    public Path generarReporte(Long solicitanteId, Long perfilId, Long algoritmoId, Path destino)
            throws JRException, IOException {
        Objects.requireNonNull(destino, "El destino del reporte es obligatorio.");
        exigirAcceso(solicitanteId, perfilId);
        JasperPrint reporte = prepararReporte(perfilId, algoritmoId);
        return Files.write(destino, JasperExportManager.exportReportToPdf(reporte));
    }

    public JasperPrint prepararReporte(Long perfilId, Long algoritmoId)
            throws JRException, IOException {
        return prepararReporte(perfilId, perfilId, algoritmoId);
    }

    public JasperPrint prepararReporte(Long solicitanteId, Long perfilId, Long algoritmoId)
            throws JRException, IOException {
        exigirAcceso(solicitanteId, perfilId);
        EstadisticasDAO.validarPerfilId(perfilId);
        if (algoritmoId == null || algoritmoId <= 0) {
            throw new IllegalArgumentException("El ID del algoritmo debe ser positivo.");
        }

        Perfil perfil = perfiles.buscarPorId(perfilId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el perfil indicado."));
        Algoritmo algoritmo = algoritmos.buscarPorId(algoritmoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el algoritmo indicado."));
        EstadisticasDAO.DominioAlgoritmo dominio =
                estadisticas.obtenerDominio(perfilId, algoritmoId);
        boolean domina = dominio.porcentajeAciertos() >= UMBRAL_DOMINIO;

        JasperReport plantilla;
        try (InputStream entrada = ReporteDominioService.class.getResourceAsStream(PLANTILLA)) {
            if (entrada == null) {
                throw new IOException("No se encontró la plantilla de dominio: " + PLANTILLA);
            }
            plantilla = JasperCompileManager.compileReport(entrada);
        }

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("perfilNombre", perfil.getNombre());
        parametros.put("algoritmoNombre", algoritmo.getNombre());
        parametros.put("fechaGeneracion", LocalDate.now().format(FORMATO_FECHA));
        parametros.put("descripcion", resumenDescripcion(algoritmo.getDescripcion()));
        parametros.put("intentos", dominio.intentosCalificados());
        parametros.put("correctos", dominio.correctos());
        parametros.put("porcentaje", String.format(Locale.ROOT, "%.1f%%",
                dominio.porcentajeAciertos()));
        parametros.put("umbral", String.format(Locale.ROOT, "%.0f%%", UMBRAL_DOMINIO));
        parametros.put("domina", domina);
        parametros.put("mensajePlan", domina ? "" : crearPlan(dominio));
        return JasperFillManager.fillReport(plantilla, parametros, new JREmptyDataSource(1));
    }

    private void exigirAcceso(Long solicitanteId, Long perfilId) {
        if (autorizacion == null) {
            if (solicitanteId == null || !solicitanteId.equals(perfilId)) {
                throw new SecurityException("No tienes acceso a la constancia de otro perfil.");
            }
            return;
        }
        autorizacion.exigirAccesoAPerfil(solicitanteId, perfilId);
    }

    private static String crearPlan(EstadisticasDAO.DominioAlgoritmo dominio) {
        if (dominio.intentosCalificados() == 0) {
            return "Aún no tienes intentos evaluados en este algoritmo.\n"
                    + "Resuelve 5 ejercicios de dificultad 1 o 2 y revisa cada paso de la solución.\n"
                    + "Después, genera otra constancia para medir tu avance.";
        }
        return "Tu resultado actual es " + String.format(Locale.ROOT, "%.1f%%",
                dominio.porcentajeAciertos()) + "; la meta es " + String.format(Locale.ROOT, "%.0f%%",
                UMBRAL_DOMINIO) + ".\n"
                + "Practica 5 ejercicios de este algoritmo en dificultad 1 o 2.\n"
                + "Al terminar, revisa los pasos donde fallaste y vuelve a generar la constancia.";
    }

    private static String resumenDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return "";
        }
        String normalizada = descripcion.strip().replaceAll("\\s+", " ");
        return normalizada.length() <= 130
                ? normalizada : normalizada.substring(0, 127).stripTrailing() + "...";
    }
}
