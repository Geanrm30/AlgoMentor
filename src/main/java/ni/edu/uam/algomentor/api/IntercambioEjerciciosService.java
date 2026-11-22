package ni.edu.uam.algomentor.api;

import ni.edu.uam.algomentor.model.Algoritmo;
import ni.edu.uam.algomentor.model.Ejercicio;
import ni.edu.uam.algomentor.util.HibernateUtil;
import ni.edu.uam.algomentor.util.ValidacionesFormulario;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IntercambioEjerciciosService {

    private static final int MAX_EJERCICIOS = 500;

    public PaqueteEjercicios exportar() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<EjercicioIntercambio> ejercicios = session.createQuery(
                    "SELECT e FROM Ejercicio e JOIN FETCH e.algoritmo ORDER BY e.id", Ejercicio.class)
                    .getResultStream()
                    .map(e -> new EjercicioIntercambio(e.getAlgoritmo().getNombre(), e.getDatos(),
                            e.getDificultad(), e.getOrigen()))
                    .toList();
            return new PaqueteEjercicios(1, ejercicios);
        }
    }

    public ResultadoImportacion importar(PaqueteEjercicios paquete) {
        if (paquete == null || paquete.version() != 1 || paquete.ejercicios() == null
                || paquete.ejercicios().isEmpty() || paquete.ejercicios().size() > MAX_EJERCICIOS) {
            throw new IllegalArgumentException("El archivo debe contener entre 1 y 500 ejercicios en formato versión 1.");
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaccion = session.beginTransaction();
            try {
                Map<String, Algoritmo> algoritmos = new HashMap<>();
                session.createQuery("FROM Algoritmo", Algoritmo.class).getResultList()
                        .forEach(a -> algoritmos.put(a.getNombre(), a));
                Set<EjercicioIntercambio> existentes = new HashSet<>();
                session.createQuery("SELECT e FROM Ejercicio e JOIN FETCH e.algoritmo", Ejercicio.class)
                        .getResultList().forEach(e -> existentes.add(new EjercicioIntercambio(
                                e.getAlgoritmo().getNombre(), e.getDatos(), e.getDificultad(), e.getOrigen())));
                int importados = 0;
                int repetidos = 0;
                for (EjercicioIntercambio dato : paquete.ejercicios()) {
                    if (dato == null || dato.algoritmo() == null || dato.algoritmo().isBlank()
                            || dato.datos() == null || dato.datos().length() > 10_000) {
                        throw new IllegalArgumentException("Hay un ejercicio sin algoritmo o con datos inválidos.");
                    }
                    Algoritmo algoritmo = algoritmos.get(dato.algoritmo());
                    if (algoritmo == null) {
                        throw new IllegalArgumentException("No existe el algoritmo '" + dato.algoritmo()
                                + "'. Créalo antes de importar el lote.");
                    }
                    ValidacionesFormulario.validarEjercicio(algoritmo, dato.origen(),
                            dato.dificultad(), dato.datos());
                    if (!existentes.add(dato)) {
                        repetidos++;
                        continue;
                    }
                    Ejercicio ejercicio = new Ejercicio();
                    ejercicio.setAlgoritmo(algoritmo);
                    ejercicio.setDatos(dato.datos());
                    ejercicio.setDificultad(dato.dificultad());
                    ejercicio.setOrigen(dato.origen());
                    session.persist(ejercicio);
                    importados++;
                }
                transaccion.commit();
                return new ResultadoImportacion(importados, repetidos);
            } catch (RuntimeException e) {
                if (transaccion.isActive()) {
                    transaccion.rollback();
                }
                throw e;
            }
        }
    }
}
