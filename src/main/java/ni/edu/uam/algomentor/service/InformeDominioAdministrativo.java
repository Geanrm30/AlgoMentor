package ni.edu.uam.algomentor.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import ni.edu.uam.algomentor.dao.AlgoritmoDAO;
import ni.edu.uam.algomentor.model.Algoritmo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Adapta {@link ReporteDominioService} al panel de Administración para que también pueda abrirse con
 * vista previa y exportación desde {@code Administración > Informes}.
 */
public class InformeDominioAdministrativo implements InformeAdministrativo {

    private final Supplier<ReporteDominioService> servicioDominio;
    private final Supplier<AlgoritmoDAO> algoritmos;
    private final Supplier<AutorizacionService> autorizacion;

    /** Crear el registro del panel no abre conexiones ni consulta PostgreSQL. */
    public InformeDominioAdministrativo() {
        this.servicioDominio = perezoso(ReporteDominioService::new);
        this.algoritmos = perezoso(AlgoritmoDAO::new);
        this.autorizacion = perezoso(AutorizacionService::new);
    }

    InformeDominioAdministrativo(ReporteDominioService servicioDominio, AlgoritmoDAO algoritmos,
                                 AutorizacionService autorizacion) {
        Objects.requireNonNull(servicioDominio);
        Objects.requireNonNull(algoritmos);
        Objects.requireNonNull(autorizacion);
        this.servicioDominio = () -> servicioDominio;
        this.algoritmos = () -> algoritmos;
        this.autorizacion = () -> autorizacion;
    }

    private static <T> Supplier<T> perezoso(Supplier<T> origen) {
        return new Supplier<>() {
            private T valor;

            @Override
            public synchronized T get() {
                if (valor == null) {
                    valor = origen.get();
                }
                return valor;
            }
        };
    }

    @Override
    public String id() {
        return "constancia-dominio";
    }

    @Override
    public String titulo() {
        return "Constancia de dominio y plan de estudio";
    }

    @Override
    public String descripcion() {
        return "Diploma de una página que certifica el dominio de un algoritmo (80 % o más de aciertos) "
                + "o genera un plan de estudio personalizado para el perfil seleccionado.";
    }

    @Override
    public List<String> secciones() {
        return List.of(
                "Reconocimiento de dominio del algoritmo seleccionado o plan de estudio personalizado.",
                "Porcentaje de aciertos sobre intentos calificados e intentos totales.",
                "Umbral requerido (80 %) y recomendaciones prácticas si aún no se alcanza."
        );
    }

    @Override
    public String nombreArchivoSugerido() {
        return "constancia-dominio-algomentor.pdf";
    }

    @Override
    public JasperPrint prepararReporte(Long solicitanteId) throws JRException, IOException {
        autorizacion.get().exigirAdministrador(solicitanteId);
        List<Algoritmo> disponibles = algoritmos.get().listar();
        if (disponibles.isEmpty()) {
            throw new IllegalStateException("No hay algoritmos registrados para generar la constancia.");
        }
        return servicioDominio.get().prepararReporte(solicitanteId, solicitanteId, disponibles.getFirst().getId());
    }

    public JasperPrint prepararReporte(Long solicitanteId, Long perfilId, Long algoritmoId)
            throws JRException, IOException {
        autorizacion.get().exigirAdministrador(solicitanteId);
        return servicioDominio.get().prepararReporte(solicitanteId, perfilId, algoritmoId);
    }

    public Path guardarPdf(Long solicitanteId, Long perfilId, Long algoritmoId, Path destino)
            throws JRException, IOException {
        Objects.requireNonNull(destino, "El destino del reporte es obligatorio.");
        return Files.write(destino, JasperExportManager.exportReportToPdf(
                prepararReporte(solicitanteId, perfilId, algoritmoId)));
    }
}
