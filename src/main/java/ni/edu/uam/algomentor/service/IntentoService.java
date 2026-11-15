
package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.TipoError;
import ni.edu.uam.algomentor.model.*;
import ni.edu.uam.algomentor.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class IntentoService {

    private final SessionFactory factory;
    private final ProgresoHabilidadService progresoService;

    public IntentoService() {
        this(HibernateUtil.getSessionFactory());
    }

    public IntentoService(SessionFactory factory) {
        this.factory = Objects.requireNonNull(factory);
        this.progresoService = new ProgresoHabilidadService();
    }

    public record ErrorRegistrado(
            Long habilidadId,
            TipoError tipo,
            String descripcion
    ) {}

    public record PasoRegistrado(
            int numero,
            String respuesta,
            boolean correcto,
            Long habilidadEvaluadaId,
            List<ErrorRegistrado> errores
    ) {

        // Conserva compatibilidad con pruebas anteriores.
        public PasoRegistrado(
                int numero,
                String respuesta,
                boolean correcto
        ) {
            this(numero, respuesta, correcto, null, List.of());
        }

        public PasoRegistrado(
                int numero,
                String respuesta,
                boolean correcto,
                List<ErrorRegistrado> errores
        ) {
            this(numero, respuesta, correcto, null, errores);
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

        Map<Long, ProgresoHabilidadService.Resultado> resultados =
                calcularResultados(modo, pasos);

        try (Session session = factory.openSession()) {

            Transaction tx = session.beginTransaction();

            try {

                Perfil perfil = session.find(
                        Perfil.class, perfilId
                );

                if (perfil == null) {
                    throw new IllegalArgumentException(
                            "El perfil no existe."
                    );
                }

                Ejercicio ejercicio = session.find(
                        Ejercicio.class, ejercicioId
                );

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

                // Construir los pasos y sus errores.
                for (PasoRegistrado dato : pasos) {

                    PasoIntento paso = new PasoIntento();

                    paso.setIntento(intento);
                    paso.setNumeroPaso(dato.numero());
                    paso.setRespuesta(dato.respuesta());
                    paso.setCorrecto(dato.correcto());

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

                        paso.getErrores().add(errorDetectado);
                    }

                    intento.getPasos().add(paso);
                }

                // Guardar intento, pasos y errores.
                session.persist(intento);

                // Actualizar las habilidades evaluadas
                // usando la MISMA sesion y transaccion.
                if (!resultados.isEmpty()) {
                    progresoService.actualizarProgreso(
                            session,
                            perfil,
                            resultados
                    );
                }

                // Confirmar toda la operacion.
                tx.commit();

                return intento;

            } catch (RuntimeException e) {

                // Si falla cualquier operacion,
                // revertir tambien las anteriores.
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

    // Calcula el progreso sin consultar PostgreSQL.
    // Los pasos sin habilidad identificada no generan
    // aciertos artificiales.
    static Map<Long, ProgresoHabilidadService.Resultado>
    calcularResultados(
            String modo,
            List<PasoRegistrado> pasos
    ) {

        if ("SIMULACION".equals(modo)) {
            return Map.of();
        }

        if (!"PRACTICA".equals(modo)) {
            throw new IllegalArgumentException(
                    "Modo de aprendizaje invalido."
            );
        }

        Objects.requireNonNull(pasos);

        Map<Long, int[]> contadores = new HashMap<>();

        for (PasoRegistrado paso : pasos) {

            // Evitar contar varias veces la misma
            // habilidad dentro de un solo paso.
            Set<Long> contabilizadas = new HashSet<>();

            Long habilidadId = paso.habilidadEvaluadaId();

            if (habilidadId != null) {

                int[] valores = contadores.computeIfAbsent(
                        habilidadId,
                        id -> new int[2]
                );

                if (paso.correcto()) {
                    valores[0] = Math.addExact(valores[0], 1);
                } else {
                    valores[1] = Math.addExact(valores[1], 1);
                }

                contabilizadas.add(habilidadId);
            }

            // Registrar errores de otras habilidades.
            // Cada habilidad se cuenta una vez por paso.
            if (!paso.correcto()) {

                for (ErrorRegistrado error : paso.errores()) {

                    if (contabilizadas.add(error.habilidadId())) {

                        int[] valores = contadores.computeIfAbsent(
                                error.habilidadId(),
                                id -> new int[2]
                        );

                        valores[1] = Math.addExact(
                                valores[1], 1
                        );
                    }
                }
            }
        }

        Map<Long, ProgresoHabilidadService.Resultado> resumen =
                new HashMap<>();

        contadores.forEach((id, valores) ->
                resumen.put(
                        id,
                        new ProgresoHabilidadService.Resultado(
                                valores[0],
                                valores[1]
                        )
                )
        );

        return Map.copyOf(resumen);
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
                    "El ID del perfil no es valido."
            );
        }

        if (ejercicioId == null || ejercicioId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del ejercicio no es valido."
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
                    "La duracion no puede ser negativa."
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
                        "Los pasos deben tener numeros positivos."
                );
            }

            if (!numeros.add(paso.numero())) {
                throw new IllegalArgumentException(
                        "Hay numeros de paso duplicados."
                );
            }

            if (paso.habilidadEvaluadaId() != null
                    && paso.habilidadEvaluadaId() <= 0) {
                throw new IllegalArgumentException(
                        "La habilidad evaluada debe tener un ID positivo."
                );
            }

            if (paso.correcto() && !paso.errores().isEmpty()) {
                throw new IllegalArgumentException(
                        "Un paso correcto no debe registrar errores."
                );
            }

            for (ErrorRegistrado error : paso.errores()) {

                if (error.habilidadId() == null
                        || error.habilidadId() <= 0) {
                    throw new IllegalArgumentException(
                            "El ID de la habilidad debe ser positivo."
                    );
                }

                if (error.tipo() == null) {
                    throw new IllegalArgumentException(
                            "El tipo de error es obligatorio."
                    );
                }

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
