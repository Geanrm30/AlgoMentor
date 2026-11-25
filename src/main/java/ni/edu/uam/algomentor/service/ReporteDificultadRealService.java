package ni.edu.uam.algomentor.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import ni.edu.uam.algomentor.dao.DificultadRealDAO;
import ni.edu.uam.algomentor.dao.DificultadRealDAO.EstadisticaEjercicio;
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

import static ni.edu.uam.algomentor.service.CalibracionDificultad.Diagnostico.*;

/** Informe administrativo de solo lectura; nunca modifica la dificultad guardada. */
public class ReporteDificultadRealService implements InformeAdministrativo {

    private static final String PLANTILLA = "/ni/edu/uam/algomentor/report/dificultad-real.jrxml";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"));

    @FunctionalInterface
    public interface ProveedorDificultadReal {
        List<EstadisticaEjercicio> listarEjercicios();
    }

    @FunctionalInterface
    public interface ProveedorPerfil {
        Optional<Perfil> buscarPorId(Long perfilId);
    }

    private final Supplier<ProveedorDificultadReal> datos;
    private final Supplier<ProveedorPerfil> perfiles;
    private final Supplier<AutorizacionService> autorizacion;

    /** Crear el registro del panel no abre conexiones ni consulta PostgreSQL. */
    public ReporteDificultadRealService() {
        this.datos = perezoso(() -> new DificultadRealDAO()::listarEjercicios);
        this.perfiles = perezoso(() -> new PerfilService()::buscarPorId);
        this.autorizacion = perezoso(AutorizacionService::new);
    }

    ReporteDificultadRealService(ProveedorDificultadReal datos, ProveedorPerfil perfiles,
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
        return "dificultad-real";
    }

    @Override
    public String titulo() {
        return "Dificultad real de los ejercicios";
    }

    @Override
    public String descripcion() {
        return "Contrasta la dificultad asignada con el acierto en práctica evaluada y señala posibles "
                + "desajustes mediante reglas heurísticas ajustables, no científicas.";
    }

    @Override
    public List<String> secciones() {
        return List.of(
                "Resumen general: ejercicios por diagnóstico, intentos evaluados, aciertos y errores.",
                "Tabla de todos los ejercicios: ID, algoritmo, nivel, intentos, aciertos, porcentaje y diagnóstico.",
                "Reglas provisionales de calibración; los ejercicios mal calibrados se resaltan en rojo.");
    }

    @Override
    public String nombreArchivoSugerido() {
        return "dificultad-real-algomentor.pdf";
    }

    @Override
    public JasperPrint prepararReporte(Long solicitanteId) throws JRException, IOException {
        autorizacion.get().exigirAdministrador(solicitanteId);

        List<EstadisticaEjercicio> ejercicios = datos.get().listarEjercicios();
        List<Map<String, ?>> filas = new ArrayList<>();
        long bien = 0;
        long mal = 0;
        for (EstadisticaEjercicio ejercicio : ejercicios) {
            var diagnostico = CalibracionDificultad.clasificar(ejercicio);
            if (diagnostico == BIEN_CALIBRADO) {
                bien++;
            } else if (diagnostico == MAL_CALIBRADO) {
                mal++;
            }
            filas.add(Map.of(
                    "id", ejercicio.id(), "algoritmo", ejercicio.algoritmo(),
                    "nivel", ejercicio.dificultad(), "intentos", ejercicio.intentos(),
                    "aciertos", ejercicio.aciertos(), "porcentaje", ejercicio.intentos() == 0 ? "—"
                            : FilasInforme.porcentaje(ejercicio.porcentajeAciertos()),
                    "diagnostico", diagnostico.etiqueta(), "malCalibrado", diagnostico == MAL_CALIBRADO));
        }

        long intentos = ejercicios.stream().mapToLong(EstadisticaEjercicio::intentos).sum();
        long aciertos = ejercicios.stream().mapToLong(EstadisticaEjercicio::aciertos).sum();
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("solicitante", perfiles.get().buscarPorId(solicitanteId).map(Perfil::getNombre).orElse("—"));
        parametros.put("fechaGeneracion", LocalDateTime.now().format(FORMATO_FECHA));
        parametros.put("totalEjercicios", (long) ejercicios.size());
        parametros.put("bienCalibrados", bien);
        parametros.put("malCalibrados", mal);
        parametros.put("datosInsuficientes", ejercicios.size() - bien - mal);
        parametros.put("totalIntentos", intentos);
        parametros.put("totalAciertos", aciertos);
        parametros.put("totalErrores", ejercicios.stream().mapToLong(EstadisticaEjercicio::errores).sum());
        parametros.put("porcentajeGlobal", intentos == 0 ? "—" : FilasInforme.porcentaje(aciertos * 100.0 / intentos));
        parametros.put("criterios", CalibracionDificultad.descripcionReglas());

        JasperReport plantilla;
        try (InputStream entrada = ReporteDificultadRealService.class.getResourceAsStream(PLANTILLA)) {
            if (entrada == null) {
                throw new IOException("No se encontró la plantilla de dificultad real: " + PLANTILLA);
            }
            plantilla = JasperCompileManager.compileReport(entrada);
        }
        return JasperFillManager.fillReport(plantilla, parametros, new JRMapCollectionDataSource(filas));
    }
}
