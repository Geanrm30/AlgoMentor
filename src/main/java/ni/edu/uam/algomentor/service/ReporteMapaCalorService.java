package ni.edu.uam.algomentor.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import ni.edu.uam.algomentor.dao.MapaCalorErroresDAO;
import ni.edu.uam.algomentor.dao.MapaCalorErroresDAO.ErroresPerfil;
import ni.edu.uam.algomentor.model.Perfil;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Informe administrativo que genera un mapa de calor cruzando cada perfil con las cuatro categorías
 * de error registradas en práctica. Solo los administradores pueden prepararlo o exportarlo.
 */
public class ReporteMapaCalorService implements InformeAdministrativo {

    private static final String PLANTILLA = "/ni/edu/uam/algomentor/report/mapa-calor.jrxml";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"));

    @FunctionalInterface
    public interface ProveedorMapaCalor {
        List<ErroresPerfil> listarErroresPorPerfil();
    }

    @FunctionalInterface
    public interface ProveedorPerfil {
        Optional<Perfil> buscarPorId(Long perfilId);
    }

    private final Supplier<ProveedorMapaCalor> datos;
    private final Supplier<ProveedorPerfil> perfiles;
    private final Supplier<AutorizacionService> autorizacion;

    /** Crear el registro del panel no abre conexiones ni consulta PostgreSQL. */
    public ReporteMapaCalorService() {
        this.datos = perezoso(() -> new MapaCalorErroresDAO()::listarErroresPorPerfil);
        this.perfiles = perezoso(() -> new PerfilService()::buscarPorId);
        this.autorizacion = perezoso(AutorizacionService::new);
    }

    ReporteMapaCalorService(ProveedorMapaCalor datos, ProveedorPerfil perfiles,
                            AutorizacionService autorizacion) {
        Objects.requireNonNull(datos);
        Objects.requireNonNull(perfiles);
        Objects.requireNonNull(autorizacion);
        this.datos = () -> datos;
        this.perfiles = () -> perfiles;
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
        return "mapa-calor-errores";
    }

    @Override
    public String titulo() {
        return "Mapa de calor de errores por perfil";
    }

    @Override
    public String descripcion() {
        return "Matriz visual de errores detectados (signo, fracción, aritmético y operación inválida) "
                + "por perfil, resaltando con escala de intensidad los focos críticos del grupo.";
    }

    @Override
    public List<String> secciones() {
        return List.of(
                "Tarjetas de resumen: perfiles evaluados, total de errores, categoría crítica y perfiles sin errores.",
                "Totales globales por tipo de error y orientación docente según los focos detectados.",
                "Matriz mapa de calor por perfil con niveles de intensidad (sin errores, bajo, medio y alto)."
        );
    }

    @Override
    public String nombreArchivoSugerido() {
        return "mapa-calor-errores-algomentor.pdf";
    }

    @Override
    public JasperPrint prepararReporte(Long solicitanteId) throws JRException, IOException {
        autorizacion.get().exigirAdministrador(solicitanteId);

        List<ErroresPerfil> registros = datos.get().listarErroresPorPerfil();
        long maxCelda = registros.stream().mapToLong(ErroresPerfil::maximoTipo).max().orElse(0L);
        long totalErrores = registros.stream().mapToLong(ErroresPerfil::total).sum();
        long totalSigno = registros.stream().mapToLong(ErroresPerfil::signo).sum();
        long totalFraccion = registros.stream().mapToLong(ErroresPerfil::fraccion).sum();
        long totalAritmetico = registros.stream().mapToLong(ErroresPerfil::aritmetico).sum();
        long totalOperacion = registros.stream().mapToLong(ErroresPerfil::operacionInvalida).sum();
        long conErrores = registros.stream().filter(r -> r.total() > 0).count();
        long sinErrores = registros.size() - conErrores;

        List<Map<String, ?>> filas = new ArrayList<>();
        for (ErroresPerfil item : registros) {
            long totalPerfil = item.total();
            String pesoGrupo = totalErrores == 0
                    ? "0.0%"
                    : FilasInforme.porcentaje(totalPerfil * 100.0 / totalErrores);
            Map<String, Object> fila = new HashMap<>();
            fila.put("perfil", item.perfil());
            fila.put("signo", item.signo());
            fila.put("nivelSigno", calcularNivel(item.signo(), maxCelda));
            fila.put("fraccion", item.fraccion());
            fila.put("nivelFraccion", calcularNivel(item.fraccion(), maxCelda));
            fila.put("aritmetico", item.aritmetico());
            fila.put("nivelAritmetico", calcularNivel(item.aritmetico(), maxCelda));
            fila.put("operacionInvalida", item.operacionInvalida());
            fila.put("nivelOperacionInvalida", calcularNivel(item.operacionInvalida(), maxCelda));
            fila.put("total", totalPerfil);
            fila.put("pesoGrupo", pesoGrupo);
            filas.add(fila);
        }

        String errorPredominante = identificarErrorPredominante(
                totalSigno, totalFraccion, totalAritmetico, totalOperacion);
        String perfilCritico = registros.stream()
                .filter(r -> r.total() > 0)
                .findFirst()
                .map(r -> r.perfil() + " (" + r.total() + ")")
                .orElse("Ninguno");

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("solicitante", perfiles.get().buscarPorId(solicitanteId).map(Perfil::getNombre).orElse("—"));
        parametros.put("fechaGeneracion", LocalDateTime.now().format(FORMATO_FECHA));
        parametros.put("totalPerfiles", (long) registros.size());
        parametros.put("totalErrores", totalErrores);
        parametros.put("perfilesConErrores", conErrores);
        parametros.put("perfilesSinErrores", sinErrores);
        parametros.put("totalSigno", totalSigno);
        parametros.put("totalFraccion", totalFraccion);
        parametros.put("totalAritmetico", totalAritmetico);
        parametros.put("totalOperacionInvalida", totalOperacion);
        parametros.put("errorPredominante", errorPredominante);
        parametros.put("perfilCritico", perfilCritico);
        parametros.put("recomendacionDocente", construirRecomendacion(
                registros.size(), totalErrores, errorPredominante, perfilCritico));

        JasperReport plantilla;
        try (InputStream entrada = ReporteMapaCalorService.class.getResourceAsStream(PLANTILLA)) {
            if (entrada == null) {
                throw new IOException("No se encontró la plantilla del mapa de calor: " + PLANTILLA);
            }
            plantilla = JasperCompileManager.compileReport(entrada);
        }
        return JasperFillManager.fillReport(plantilla, parametros, new JRMapCollectionDataSource(filas));
    }

    /**
     * Calcula la intensidad cromática de una celda en escala de 0 (sin errores) a 3 (foco crítico),
     * usando un piso de 3 para evitar que un único error aislado se marque como crítico.
     */
    static int calcularNivel(long cantidad, long maxCelda) {
        if (cantidad <= 0 || maxCelda <= 0) {
            return 0;
        }
        double escala = Math.max(maxCelda, 3L);
        double proporcion = cantidad / escala;
        if (proporcion <= 0.34) {
            return 1;
        }
        if (proporcion <= 0.67) {
            return 2;
        }
        return 3;
    }

    private static String identificarErrorPredominante(long signo, long fraccion, long aritmetico, long opInvalida) {
        long maximo = Math.max(Math.max(signo, fraccion), Math.max(aritmetico, opInvalida));
        if (maximo == 0) {
            return "Ninguno";
        }
        List<String> empatados = new ArrayList<>();
        if (signo == maximo) empatados.add("Signos");
        if (fraccion == maximo) empatados.add("Fracciones");
        if (aritmetico == maximo) empatados.add("Aritmético");
        if (opInvalida == maximo) empatados.add("Op. inválida");
        return String.join(" / ", empatados);
    }

    private static String construirRecomendacion(int totalPerfiles, long totalErrores,
                                                 String errorPredominante, String perfilCritico) {
        if (totalPerfiles == 0) {
            return "Aún no existen perfiles de estudiantes registrados en el sistema.";
        }
        if (totalErrores == 0) {
            return "El grupo no registra errores detectados en práctica hasta la fecha. "
                    + "Se sugiere asignar ejercicios de nivel 3 a 5 para validar dominio avanzado.";
        }
        return "Foco grupal principal: " + errorPredominante + ". Perfil con mayor carga de errores: "
                + perfilCritico + ". Se recomienda reforzar en clase las operaciones vinculadas a "
                + errorPredominante.toLowerCase(Locale.ROOT)
                + " y realizar seguimiento individual en los perfiles con celdas naranjas o rojas.";
    }
}
