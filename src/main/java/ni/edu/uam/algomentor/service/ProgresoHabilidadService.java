
package ni.edu.uam.algomentor.service;

import jakarta.persistence.LockModeType;

import ni.edu.uam.algomentor.model.Habilidad;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.model.ProgresoHabilidad;

import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.Map;

public class ProgresoHabilidadService {

    // Resultado de las evaluaciones de una habilidad.
    public record Resultado(
            int aciertos,
            int errores
    ) {
        public Resultado {
            if (aciertos < 0 || errores < 0) {
                throw new IllegalArgumentException(
                        "Los contadores no pueden ser negativos."
                );
            }

            if (aciertos == 0 && errores == 0) {
                throw new IllegalArgumentException(
                        "Debe existir al menos una evaluacion."
                );
            }
        }
    }

    // Validaciones independientes de PostgreSQL.
    public static void validarResultados(
            Map<Long, Resultado> resultados
    ) {
        if (resultados == null) {
            throw new IllegalArgumentException(
                    "Los resultados no pueden ser nulos."
            );
        }

        for (Map.Entry<Long, Resultado> entrada
                : resultados.entrySet()) {

            Long habilidadId = entrada.getKey();

            if (habilidadId == null || habilidadId <= 0) {
                throw new IllegalArgumentException(
                        "El ID de la habilidad debe ser positivo."
                );
            }

            if (entrada.getValue() == null) {
                throw new IllegalArgumentException(
                        "El resultado de una habilidad no puede ser nulo."
                );
            }
        }
    }

    // Actualiza los resultados utilizando la transaccion
    // existente. No abre otra sesion ni realiza commit.
    public void actualizarProgreso(
            Session session,
            Perfil perfil,
            Map<Long, Resultado> resultados
    ) {

        validarResultados(resultados);

        if (session == null || !session.isOpen()) {
            throw new IllegalArgumentException(
                    "La sesion de Hibernate no esta disponible."
            );
        }

        if (session.getTransaction() == null
                || !session.getTransaction().isActive()) {
            throw new IllegalStateException(
                    "Se necesita una transaccion activa."
            );
        }

        if (perfil == null || perfil.getId() == null
                || perfil.getId() <= 0) {
            throw new IllegalArgumentException(
                    "El perfil debe existir."
            );
        }

        for (Map.Entry<Long, Resultado> entrada
                : resultados.entrySet()) {

            Long habilidadId = entrada.getKey();
            Resultado resultado = entrada.getValue();

            // Buscar progreso existente y bloquearlo
            // mientras se actualizan los contadores.
            ProgresoHabilidad progreso = session.createQuery(
                            """
                            FROM ProgresoHabilidad p
                            WHERE p.perfil.id = :perfilId
                            AND p.habilidad.id = :habilidadId
                            """,
                            ProgresoHabilidad.class
                    )
                    .setParameter("perfilId", perfil.getId())
                    .setParameter("habilidadId", habilidadId)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .uniqueResultOptional()
                    .orElse(null);

            // Crear registro si es la primera evaluacion
            // de esta habilidad para el estudiante.
            if (progreso == null) {

                Habilidad habilidad = session.find(
                        Habilidad.class,
                        habilidadId
                );

                if (habilidad == null) {
                    throw new IllegalArgumentException(
                            "La habilidad no existe: " + habilidadId
                    );
                }

                progreso = new ProgresoHabilidad();
                progreso.setPerfil(perfil);
                progreso.setHabilidad(habilidad);
                progreso.setAciertos(0);
                progreso.setErrores(0);
            }

            // Acumular los resultados anteriores.
            progreso.setAciertos(
                    Math.addExact(
                            progreso.getAciertos(),
                            resultado.aciertos()
                    )
            );

            progreso.setErrores(
                    Math.addExact(
                            progreso.getErrores(),
                            resultado.errores()
                    )
            );

            progreso.setUltimaActualizacion(
                    LocalDateTime.now()
            );

            // Guarda un registro nuevo o actualiza
            // la entidad existente administrada.
            if (progreso.getId() == null) {
                session.persist(progreso);
            }
        }
    }
}
