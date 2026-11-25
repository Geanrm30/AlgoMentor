package ni.edu.uam.algomentor.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import ni.edu.uam.algomentor.dao.CatalogoDAO;
import ni.edu.uam.algomentor.model.Perfil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Informe PDF del contenido de AlgoMentor: algoritmos, ejercicios por dificultad y origen, y los más
 * usados. Es un informe global, así que solo lo puede pedir un administrador.
 */
public class ReporteCatalogoService implements InformeAdministrativo {

    private static final String PLANTILLA = "/ni/edu/uam/algomentor/report/catalogo.jrxml";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"));

    public interface ProveedorCatalogo {
        List<CatalogoDAO.AlgoritmoCatalogo> listarAlgoritmos();

        List<CatalogoDAO.ConteoClave> listarEjerciciosPorDificultad();

        List<CatalogoDAO.ConteoClave> listarEjerciciosPorOrigen();

        List<CatalogoDAO.EjercicioUsado> listarEjerciciosMasUsados(int limite);
    }

    @FunctionalInterface
    public interface ProveedorPerfil {
        Optional<Perfil> buscarPorId(Long perfilId);
    }

    private final Supplier<ProveedorCatalogo> catalogo;
    private final Supplier<ProveedorPerfil> perfiles;
    private final Supplier<AutorizacionService> autorizacion;

    /** Las dependencias con base de datos se crean al generar el informe, no al armar el panel. */
    public ReporteCatalogoService() {
        this.catalogo = perezoso(() -> crearProveedor(new CatalogoDAO()));
        this.perfiles = perezoso(() -> new PerfilService()::buscarPorId);
        this.autorizacion = perezoso(AutorizacionService::new);
    }

    ReporteCatalogoService(ProveedorCatalogo catalogo, ProveedorPerfil perfiles,
                           AutorizacionService autorizacion) {
        Objects.requireNonNull(catalogo);
        Objects.requireNonNull(perfiles);
        Objects.requireNonNull(autorizacion);
        this.catalogo = () -> catalogo;
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
        return "catalogo";
    }

    @Override
    public String titulo() {
        return "Catálogo de algoritmos y ejercicios";
    }

    @Override
    public String descripcion() {
        return "Algoritmos con sus ejercicios, intentos y porcentaje de acierto; ejercicios por dificultad y "
                + "por origen, y los diez ejercicios más usados.";
    }

    @Override
    public List<String> secciones() {
        return List.of(
                "Ocho tarjetas con el resumen: algoritmos, ejercicios, intentos, acierto global y dificultad media.",
                "Algoritmos del catálogo con sus ejercicios, intentos y porcentaje de acierto.",
                "Ejercicios por dificultad y por origen (aleatorio, manual o personalizado).",
                "Los diez ejercicios más usados.");
    }

    @Override
    public String nombreArchivoSugerido() {
        return "catalogo-algomentor.pdf";
    }

    /**
     * @throws SecurityException si el solicitante no es administrador.
     */
    public Path generarReporte(Long solicitanteId, Path destino) throws JRException, IOException {
        return guardarPdf(solicitanteId, destino);
    }

    @Override
    public JasperPrint prepararReporte(Long solicitanteId) throws JRException, IOException {
        autorizacion.get().exigirAdministrador(solicitanteId);

        ProveedorCatalogo datos = catalogo.get();
        List<CatalogoDAO.AlgoritmoCatalogo> algoritmos = datos.listarAlgoritmos();
        List<CatalogoDAO.ConteoClave> porDificultad = datos.listarEjerciciosPorDificultad();
        List<CatalogoDAO.ConteoClave> porOrigen = datos.listarEjerciciosPorOrigen();
        List<CatalogoDAO.EjercicioUsado> masUsados =
                datos.listarEjerciciosMasUsados(FilasCatalogo.EJERCICIOS_EN_INFORME);
        List<Map<String, ?>> filas = FilasCatalogo.construir(algoritmos, porDificultad, porOrigen, masUsados);

        long ordenamiento = algoritmos.stream().filter(a -> "ORDENAMIENTO".equals(a.categoria())).count();
        long matematicos = algoritmos.size() - ordenamiento;
        long totalEjercicios = porDificultad.stream().mapToLong(CatalogoDAO.ConteoClave::cantidad).sum();
        long totalIntentos = algoritmos.stream().mapToLong(CatalogoDAO.AlgoritmoCatalogo::intentos).sum();
        long totalCorrectos = algoritmos.stream().mapToLong(CatalogoDAO.AlgoritmoCatalogo::correctos).sum();
        long sinEjercicios = algoritmos.stream().filter(a -> a.ejercicios() == 0).count();
        double sumaDificultad = porDificultad.stream()
                .mapToDouble(c -> parsearNivel(c.clave()) * c.cantidad()).sum();
        String solicitante = perfiles.get().buscarPorId(solicitanteId).map(Perfil::getNombre).orElse("—");

        JasperReport plantilla;
        try (InputStream entrada = ReporteCatalogoService.class.getResourceAsStream(PLANTILLA)) {
            if (entrada == null) {
                throw new IOException("No se encontró la plantilla del catálogo: " + PLANTILLA);
            }
            plantilla = JasperCompileManager.compileReport(entrada);
        }

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("solicitante", solicitante);
        parametros.put("fechaGeneracion", LocalDateTime.now().format(FORMATO_FECHA));
        parametros.put("totalAlgoritmos", (long) algoritmos.size());
        parametros.put("algoritmosOrdenamiento", ordenamiento);
        parametros.put("algoritmosMatematicos", matematicos);
        parametros.put("totalEjercicios", totalEjercicios);
        parametros.put("totalIntentos", totalIntentos);
        parametros.put("porcentajeAcierto", totalIntentos == 0 ? "—"
                : FilasInforme.porcentaje(totalCorrectos * 100.0 / totalIntentos));
        parametros.put("dificultadPromedio", totalEjercicios == 0 ? "—"
                : String.format(Locale.ROOT, "%.1f", sumaDificultad / totalEjercicios));
        parametros.put("algoritmosSinEjercicios", sinEjercicios);
        return JasperFillManager.fillReport(plantilla, parametros, new JRMapCollectionDataSource(filas));
    }

    private static int parsearNivel(String clave) {
        try {
            return Integer.parseInt(clave);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static ProveedorCatalogo crearProveedor(CatalogoDAO dao) {
        return new ProveedorCatalogo() {
            @Override
            public List<CatalogoDAO.AlgoritmoCatalogo> listarAlgoritmos() {
                return dao.listarAlgoritmos();
            }

            @Override
            public List<CatalogoDAO.ConteoClave> listarEjerciciosPorDificultad() {
                return dao.listarEjerciciosPorDificultad();
            }

            @Override
            public List<CatalogoDAO.ConteoClave> listarEjerciciosPorOrigen() {
                return dao.listarEjerciciosPorOrigen();
            }

            @Override
            public List<CatalogoDAO.EjercicioUsado> listarEjerciciosMasUsados(int limite) {
                return dao.listarEjerciciosMasUsados(limite);
            }
        };
    }
}
