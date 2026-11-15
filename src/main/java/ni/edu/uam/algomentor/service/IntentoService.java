
package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.TipoError;
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
        this.factory = Objects.requireNonNull(
                factory,
                "SessionFactory no puede ser nula."
        );
    }

    // Representa un error cometido por el estudiante.
    public record ErrorRegistrado(
            Long habilidadId,
            TipoError tipo,
            String descripcion
    ) {}

    // Representa un paso realizado durante un intento.
    public record PasoRegistrado(
            int numero,
            String respuesta,
            boolean correcto,
            List<ErrorRegistrado> errores
    ) {

        // Compatibilidad con el constructor original.
        public PasoRegistrado(
                int numero,
                String respuesta,
                boolean correcto
        ) {
            this(numero, respuesta, correcto, List.of());
        }

        public PasoRegistrado {
            if (errores == null) {
                throw new IllegalArgumentException(
                        "La lista de errores no puede ser nula."
                );
            }

            if (errores.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException(
                        "La lista contiene un error nulo."
                );
            }

            errores = List.copyOf(errores);
        }
    }

    // Guarda un intento, sus pasos y sus errores
    // dentro de una misma transaccion.
    public Intento registrarIntento(
            Long perfilId,
            Long ejercicioId,
            String modo,
            boolean correcto,
            int duracionSegundos,
            List<PasoRegistrado> pasos
    ) {

        validarDatos(
                perfilId,
                ejercicioId,
                modo,
                duracionSegundos,
                pasos
        );

        try (Session session = factory.openSession()) {

            Transaction tx = session.beginTransaction();

            try {

                // Buscar el perfil del estudiante.
                Perfil perfil = session.find(
                        Perfil.class,
                        perfilId
                );

                if (perfil == null) {
                    throw new IllegalArgumentException(
                            "El perfil no existe."
                    );
                }

                // Buscar el ejercicio realizado.
                Ejercicio ejercicio = session.find(
                        Ejercicio.class,
                        ejercicioId
                );

                if (ejercicio == null) {
                    throw new IllegalArgumentException(
                            "El ejercicio no existe."
                    );
                }

                // Crear el intento.
                Intento intento = new Intento();

                intento.setPerfil(perfil);
                intento.setEjercicio(ejercicio);
                intento.setModo(modo);
                intento.setCorrecto(correcto);
                intento.setDuracionSegundos(duracionSegundos);

                // Registrar los pasos.
                for (PasoRegistrado dato : pasos) {

                    PasoIntento paso = new PasoIntento();

                    paso.setIntento(intento);
                    paso.setNumeroPaso(dato.numero());
                    paso.setRespuesta(dato.respuesta());
                    paso.setCorrecto(dato.correcto());

                    // Registrar los errores de cada paso.
                    for (ErrorRegistrado error : dato.errores()) {

                        Habilidad habilidad = session.find(
                                Habilidad.class,
                                error.habilidadId()
                        );

                        if (habilidad == null) {
                            throw new IllegalArgumentException(
                                    "No existe la habilidad con ID "
                                            + error.habilidadId()
                            );
                        }

                        ErrorDetectado errorDetectado =
                                new ErrorDetectado();

                        errorDetectado.setPasoIntento(paso);
                        errorDetectado.setHabilidad(habilidad);

                        errorDetectado.setTipoError(
                                error.tipo().name()
                        );

                        errorDetectado.setDescripcion(
                                error.descripcion()
                        );

                        // Asociar el error con su paso.
                        paso.getErrores().add(errorDetectado);
                    }

                    // Asociar el paso con el intento.
                    intento.getPasos().add(paso);
                }

                // Hibernate guarda las entidades relacionadas
                // mediante CascadeType.ALL.
                session.persist(intento);

                // Confirmar la transaccion completa.
                tx.commit();

                return intento;

            } catch (RuntimeException e) {

                // Revertir toda la operacion si ocurre un error.
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

    // Validaciones previas al acceso a PostgreSQL.
    public static void validarDatos(
            Long perfilId,
            Long ejercicioId,
            String modo,
            int duracionSegundos,
            List<PasoRegistrado> pasos
    ) {

        // Validar identificador del perfil.
        if (perfilId == null || perfilId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del perfil no es valido."
            );
        }

        // Validar identificador del ejercicio.
        if (ejercicioId == null || ejercicioId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del ejercicio no es valido."
            );
        }

        // Validar modo de aprendizaje.
        if (!"SIMULACION".equals(modo)
                && !"PRACTICA".equals(modo)) {
            throw new IllegalArgumentException(
                    "El modo debe ser SIMULACION o PRACTICA."
            );
        }

        // Validar duracion.
        if (duracionSegundos < 0) {
            throw new IllegalArgumentException(
                    "La duracion no puede ser negativa."
            );
        }

        // Validar existencia de pasos.
        if (pasos == null || pasos.isEmpty()) {
            throw new IllegalArgumentException(
                    "El intento debe contener al menos un paso."
            );
        }

        Set<Integer> numeros = new HashSet<>();

        for (PasoRegistrado paso : pasos) {

            // Validar numero de paso.
            if (paso == null || paso.numero() <= 0) {
                throw new IllegalArgumentException(
                        "Los pasos deben tener numeros positivos."
                );
            }

            // Evitar pasos duplicados.
            if (!numeros.add(paso.numero())) {
                throw new IllegalArgumentException(
                        "Hay numeros de paso duplicados."
                );
            }

            // Un paso correcto no puede registrar errores.
            if (paso.correcto() && !paso.errores().isEmpty()) {
                throw new IllegalArgumentException(
                        "Un paso correcto no debe registrar errores."
                );
            }

            // Validar cada error del paso.
            for (ErrorRegistrado error : paso.errores()) {

                // Validar habilidad relacionada.
                if (error.habilidadId() == null
                        || error.habilidadId() <= 0) {
                    throw new IllegalArgumentException(
                            "El ID de la habilidad debe ser positivo."
                    );
                }

                // Validar tipo de error.
                if (error.tipo() == null) {
                    throw new IllegalArgumentException(
                            "El tipo de error es obligatorio."
                    );
                }

                // Validar longitud de descripcion.
                if (error.descripcion() != null
                        && error.descripcion().length() > 255) {
                    throw new IllegalArgumentException(
                            "La descripcion no puede superar 255 caracteres."
                    );
                }
            }
        }
    }
}
