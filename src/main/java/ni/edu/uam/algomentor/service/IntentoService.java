
package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.model.*;
import ni.edu.uam.algomentor.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class IntentoService {

    private final SessionFactory factory;

    public IntentoService() {
        this(HibernateUtil.getSessionFactory());
    }

    public IntentoService(SessionFactory factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    public record PasoRegistrado(
            int numero,
            String respuesta,
            boolean correcto
    ) {}

    public Intento registrarIntento(
            Long perfilId,
            Long ejercicioId,
            String modo,
            boolean correcto,
            int duracionSegundos,
            List<PasoRegistrado> pasos
    ) {
        validarDatos(
                perfilId, ejercicioId, modo,
                duracionSegundos, pasos
        );

        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();

            try {
                Perfil perfil = session.find(
                        Perfil.class, perfilId
                );

                Ejercicio ejercicio = session.find(
                        Ejercicio.class, ejercicioId
                );

                if (perfil == null) {
                    throw new IllegalArgumentException(
                            "El perfil no existe."
                    );
                }

                if (ejercicio == null) {
                    throw new IllegalArgumentException(
                            "El ejercicio no existe."
                    );
                }

                Intento intento = new Intento();
                intento.setPerfil(perfil);
                intento.setEjercicio(ejercicio);
                intento.setModo(modo);
                intento.setCorrecto(correcto);
                intento.setDuracionSegundos(duracionSegundos);

                for (PasoRegistrado dato : pasos) {
                    PasoIntento paso = new PasoIntento();

                    paso.setIntento(intento);
                    paso.setNumeroPaso(dato.numero());
                    paso.setRespuesta(dato.respuesta());
                    paso.setCorrecto(dato.correcto());

                    intento.getPasos().add(paso);
                }

                session.persist(intento);
                tx.commit();

                return intento;

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

    public static void validarDatos(
            Long perfilId,
            Long ejercicioId,
            String modo,
            int duracionSegundos,
            List<PasoRegistrado> pasos
    ) {
        if (perfilId == null || perfilId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del perfil no es válido."
            );
        }

        if (ejercicioId == null || ejercicioId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del ejercicio no es válido."
            );
        }

        if (!"SIMULACION".equals(modo)
                && !"PRACTICA".equals(modo)) {
            throw new IllegalArgumentException(
                    "El modo debe ser SIMULACION o PRACTICA."
            );
        }

        if (duracionSegundos < 0) {
            throw new IllegalArgumentException(
                    "La duración no puede ser negativa."
            );
        }

        if (pasos == null || pasos.isEmpty()) {
            throw new IllegalArgumentException(
                    "El intento debe contener al menos un paso."
            );
        }

        Set<Integer> numeros = new HashSet<>();

        for (PasoRegistrado paso : pasos) {
            if (paso == null || paso.numero() <= 0) {
                throw new IllegalArgumentException(
                        "Los pasos deben tener números positivos."
                );
            }

            if (!numeros.add(paso.numero())) {
                throw new IllegalArgumentException(
                        "Hay números de paso duplicados."
                );
            }
        }
    }
}
