
package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class BaseDAO<T> implements CRUD<T> {

    private final Class<T> tipo;
    private final SessionFactory factory;

    public BaseDAO(Class<T> tipo) {
        this.tipo = tipo;
        this.factory = HibernateUtil.getSessionFactory();
    }

    @Override
    public void guardar(T entidad) {
        ejecutarTransaccion(session -> {
            session.persist(entidad);
            return null;
        });
    }

    @Override
    public T buscarPorId(Long id) {
        try (Session session = factory.openSession()) {
            return session.find(tipo, id);
        }
    }

    @Override
    public List<T> listar() {
        try (Session session = factory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> query = cb.createQuery(tipo);
            query.select(query.from(tipo));

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public T actualizar(T entidad) {
        return ejecutarTransaccion(
                session -> session.merge(entidad)
        );
    }

    @Override
    public void eliminar(Long id) {
        ejecutarTransaccion(session -> {
            T entidad = session.find(tipo, id);

            if (entidad == null) {
                throw new NoSuchElementException(
                        "No existe " + tipo.getSimpleName()
                                + " con ID " + id
                );
            }

            session.remove(entidad);
            return null;
        });
    }

    private <R> R ejecutarTransaccion(
            Function<Session, R> operacion) {

        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();

            try {
                R resultado = operacion.apply(session);
                tx.commit();
                return resultado;

            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    try {
                        tx.rollback();
                    } catch (RuntimeException rollbackError) {
                        e.addSuppressed(rollbackError);
                    }
                }
                throw e;
            }
        }
    }
}
