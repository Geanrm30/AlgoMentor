package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.CuentaDAO;
import ni.edu.uam.algomentor.model.Cuenta;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Decide quién es administrador. La consulta va a la base de datos cada vez, de modo que ocultar un
 * menú en la interfaz nunca es la única barrera: los servicios que entregan datos globales vuelven
 * a comprobar el rol antes de responder.
 */
public class AutorizacionService {

    private final Predicate<Long> consultaAdministrador;

    public AutorizacionService() {
        this(new CuentaDAO());
    }

    public AutorizacionService(CuentaDAO cuentaDAO) {
        Objects.requireNonNull(cuentaDAO);
        this.consultaAdministrador = perfilId -> cuentaDAO.tieneRol(perfilId, Cuenta.ROL_ADMIN);
    }

    AutorizacionService(Predicate<Long> consultaAdministrador) {
        this.consultaAdministrador = Objects.requireNonNull(consultaAdministrador);
    }

    public boolean esAdministrador(Long perfilId) {
        return perfilId != null && consultaAdministrador.test(perfilId);
    }

    public void exigirAdministrador(Long perfilId) {
        if (!esAdministrador(perfilId)) {
            throw new SecurityException("Esta sección es solo para administradores.");
        }
    }
}
