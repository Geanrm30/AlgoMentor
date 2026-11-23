package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.AlgoritmoDAO;
import ni.edu.uam.algomentor.dao.BaseDAO;
import ni.edu.uam.algomentor.dao.EjercicioDAO;
import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.model.Algoritmo;
import ni.edu.uam.algomentor.model.Ejercicio;
import ni.edu.uam.algomentor.model.Intento;
import ni.edu.uam.algomentor.model.Perfil;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Datos globales (todos los perfiles y todos los ejercicios) para el panel de administración. */
public class ResumenAdministracionService {

    public record Datos(List<Algoritmo> algoritmos, List<Ejercicio> ejercicios,
                        List<Perfil> perfiles, int totalIntentos, List<PuestoRanking> ranking) { }

    /** Un perfil en el ranking; {@code porcentaje} es el acierto sobre los intentos calificados. */
    public record PuestoRanking(int puesto, Long perfilId, String nombre, long intentos,
                                long correctos, long incorrectos, double porcentaje) { }

    private final AutorizacionService autorizacion;

    public ResumenAdministracionService() {
        this(new AutorizacionService());
    }

    public ResumenAdministracionService(AutorizacionService autorizacion) {
        this.autorizacion = Objects.requireNonNull(autorizacion);
    }

    /** @throws SecurityException si el perfil que consulta no es administrador. */
    public Datos cargar(Long perfilSolicitante) {
        autorizacion.exigirAdministrador(perfilSolicitante);
        return new Datos(
                new AlgoritmoDAO().listar(),
                new EjercicioDAO().listarConAlgoritmo(),
                new BaseDAO<>(Perfil.class).listar(),
                new BaseDAO<>(Intento.class).listar().size(),
                clasificar(new EstadisticasDAO().listarResumenesPerfiles()));
    }

    private static double acierto(EstadisticasDAO.ResumenIntentos r) {
        long calificados = r.correctos() + r.incorrectos();
        return calificados == 0 ? 0.0 : r.correctos() * 100.0 / calificados;
    }

    /** Ordena por aciertos, luego por porcentaje y por último por nombre; los perfiles sin intentos quedan al final. */
    static List<PuestoRanking> clasificar(List<EstadisticasDAO.ResumenPerfil> perfiles) {
        List<EstadisticasDAO.ResumenPerfil> ordenados = perfiles.stream()
                .sorted(Comparator
                        .comparingLong((EstadisticasDAO.ResumenPerfil p) -> p.intentos().correctos()).reversed()
                        .thenComparing(Comparator.comparingDouble(
                                (EstadisticasDAO.ResumenPerfil p) -> acierto(p.intentos())).reversed())
                        .thenComparing(p -> p.nombre().toLowerCase()))
                .toList();
        java.util.ArrayList<PuestoRanking> ranking = new java.util.ArrayList<>();
        for (int i = 0; i < ordenados.size(); i++) {
            EstadisticasDAO.ResumenPerfil p = ordenados.get(i);
            ranking.add(new PuestoRanking(i + 1, p.id(), p.nombre(), p.intentos().total(),
                    p.intentos().correctos(), p.intentos().incorrectos(), acierto(p.intentos())));
        }
        return List.copyOf(ranking);
    }
}
