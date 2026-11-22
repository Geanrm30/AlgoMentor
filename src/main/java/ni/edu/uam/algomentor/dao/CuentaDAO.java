package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.model.Cuenta;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class CuentaDAO {

    private final SessionFactory factory;

    public CuentaDAO() {
        this(HibernateUtil.getSessionFactory());
    }

    public CuentaDAO(SessionFactory factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    public Optional<Cuenta> buscarPorUsuario(String usuario) {
        try (Session session = factory.openSession()) {
            return session.createQuery("""
                            SELECT c
                            FROM Cuenta c
                            JOIN FETCH c.perfil
                            WHERE c.usuario = :usuario
                            """, Cuenta.class)
                    .setParameter("usuario", usuario.strip().toLowerCase(Locale.ROOT))
                    .uniqueResultOptional();
        }
    }

    public Perfil registrar(String usuario, String contrasenaHash) {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                if (buscarPorUsuario(session, usuario).isPresent()) {
                    throw new IllegalArgumentException("Ese usuario ya está registrado.");
                }
                Optional<Perfil> perfilEncontrado = session.createQuery("""
                                FROM Perfil p
                                WHERE lower(p.nombre) = :nombre
                                """, Perfil.class)
                        .setParameter("nombre", usuario)
                        .uniqueResultOptional();
                Perfil perfil = perfilEncontrado.orElseGet(() -> {
                    Perfil nuevo = new Perfil();
                    nuevo.setNombre(usuario);
                    session.persist(nuevo);
                    session.flush();
                    return nuevo;
                });
                boolean perfilVinculado = session.createQuery("""
                                SELECT COUNT(c)
                                FROM Cuenta c
                                WHERE c.perfil.id = :perfilId
                                """, Long.class)
                        .setParameter("perfilId", perfil.getId())
                        .getSingleResult() > 0;
                if (perfilVinculado) {
                    throw new IllegalArgumentException(
                            "Ese perfil ya está vinculado a otra cuenta."
                    );
                }

                if (perfil.getId() == null) {
                    throw new IllegalStateException("No se pudo crear el perfil de la cuenta.");
                }

                Cuenta cuenta = new Cuenta();
                cuenta.setUsuario(usuario);
                cuenta.setContrasenaHash(contrasenaHash);
                cuenta.setPerfil(perfil);
                session.persist(cuenta);

                transaction.commit();
                return perfil;
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    try {
                        transaction.rollback();
                    } catch (RuntimeException rollbackError) {
                        e.addSuppressed(rollbackError);
                    }
                }
                throw e;
            }
        }
    }

    private Optional<Cuenta> buscarPorUsuario(Session session, String usuario) {
        return session.createQuery("""
                        FROM Cuenta c
                        WHERE c.usuario = :usuario
                        """, Cuenta.class)
                .setParameter("usuario", usuario)
                .uniqueResultOptional();
    }
}
