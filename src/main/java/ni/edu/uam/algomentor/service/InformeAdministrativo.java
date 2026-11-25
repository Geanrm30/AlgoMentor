package ni.edu.uam.algomentor.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Un informe global que aparece en el panel de Administración. Para agregar uno nuevo basta con
 * implementar esta interfaz y registrarlo en {@link RegistroInformes}; el panel se encarga de la lista, la
 * vista previa y la exportación a PDF.
 */
public interface InformeAdministrativo {

    /** Identificador estable y único (por ejemplo {@code "catalogo"}). */
    String id();

    String titulo();

    /** Qué muestra el informe, en una o dos frases. */
    String descripcion();

    /** Apartados que incluye el PDF, para mostrarlos en el panel antes de generarlo. */
    default List<String> secciones() {
        return List.of();
    }

    /** Nombre propuesto al guardar, con la extensión {@code .pdf}. */
    String nombreArchivoSugerido();

    /**
     * Llena la plantilla con los datos actuales. Debe comprobar por sí mismo que el solicitante sea
     * administrador, porque ocultar el menú en la interfaz nunca es la única barrera.
     *
     * @throws SecurityException si el solicitante no tiene permiso.
     */
    JasperPrint prepararReporte(Long solicitanteId) throws JRException, IOException;

    default Path guardarPdf(Long solicitanteId, Path destino) throws JRException, IOException {
        Objects.requireNonNull(destino, "El destino del reporte es obligatorio.");
        return Files.write(destino, JasperExportManager.exportReportToPdf(prepararReporte(solicitanteId)));
    }
}
