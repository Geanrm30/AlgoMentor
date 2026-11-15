
package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.PerfilDAO;
import ni.edu.uam.algomentor.model.Perfil;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PerfilService {

    private final PerfilDAO perfilDAO;

    public PerfilService() {
        this(new PerfilDAO());
    }

    public PerfilService(PerfilDAO perfilDAO) {
        this.perfilDAO = Objects.requireNonNull(perfilDAO);
    }

    public Perfil crearPerfil(String nombre) {
        String nombreValido = validarNombre(nombre);

        if (perfilDAO.buscarPorNombre(nombreValido).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un perfil con ese nombre."
            );
        }

        Perfil perfil = new Perfil();
        perfil.setNombre(nombreValido);

        perfilDAO.guardar(perfil);
        return perfil;
    }

    public Optional<Perfil> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El ID del perfil debe ser positivo."
            );
        }

        return Optional.ofNullable(perfilDAO.buscarPorId(id));
    }

    public List<Perfil> listarPerfiles() {
        return perfilDAO.listar().stream()
                .sorted(Comparator.comparing(
                        Perfil::getNombre,
                        String.CASE_INSENSITIVE_ORDER
                ))
                .toList();
    }

    public static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del perfil es obligatorio."
            );
        }

        String normalizado = nombre.strip();

        if (normalizado.length() > 80) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar 80 caracteres."
            );
        }

        if (normalizado.codePoints()
                .anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(
                    "El nombre contiene caracteres no permitidos."
            );
        }

        return normalizado;
    }
}
