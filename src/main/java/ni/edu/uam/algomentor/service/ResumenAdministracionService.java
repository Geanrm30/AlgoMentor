package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.AlgoritmoDAO;
import ni.edu.uam.algomentor.dao.BaseDAO;
import ni.edu.uam.algomentor.dao.EjercicioDAO;
import ni.edu.uam.algomentor.model.Algoritmo;
import ni.edu.uam.algomentor.model.Ejercicio;
import ni.edu.uam.algomentor.model.Intento;
import ni.edu.uam.algomentor.model.Perfil;

import java.util.List;
import java.util.Objects;

/** Datos globales (todos los perfiles y todos los ejercicios) para el panel de administración. */
public class ResumenAdministracionService {

    public record Datos(List<Algoritmo> algoritmos, List<Ejercicio> ejercicios,
                        List<Perfil> perfiles, int totalIntentos) { }

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
                new BaseDAO<>(Intento.class).listar().size());
    }
}
