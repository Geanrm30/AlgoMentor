package ni.edu.uam.algomentor.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de informes del panel de Administración, en el orden en que se muestran.
 *
 * <p>Para sumar un informe: crea una clase que implemente {@link InformeAdministrativo} y agrégala con
 * {@code informes.add(...)} donde indica el comentario. La lista, la vista previa y la exportación a PDF del
 * panel funcionan sin tocar la interfaz.
 */
public final class RegistroInformes {

    private RegistroInformes() { }

    public static List<InformeAdministrativo> todos() {
        List<InformeAdministrativo> informes = new ArrayList<>();
        informes.add(new ReporteCatalogoService());
        informes.add(new ReporteDificultadRealService());
        informes.add(new ReporteMapaCalorService());
        // Agrega aquí los informes nuevos: informes.add(new MiInforme());
        return List.copyOf(informes);
    }
}
