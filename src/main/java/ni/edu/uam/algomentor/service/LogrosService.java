package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.EstadisticasDAO;

import java.util.List;

/** Logros calculados a partir de las estadísticas del perfil y de los puntajes de los retos. */
public final class LogrosService {

    public record Datos(long intentos, long correctos, long incorrectos, int rachaMaxima,
                        int algoritmosDominados, int mejorDesafio, int mejorCuestionario) { }

    public record Logro(String titulo, String descripcion, double progreso, String detalle) {
        public boolean desbloqueado() {
            return progreso >= 1.0;
        }
    }

    private LogrosService() { }

    public static List<Logro> evaluar(Datos d) {
        long calificados = d.correctos() + d.incorrectos();
        double precision = calificados == 0 ? 0 : (double) d.correctos() / calificados;
        return List.of(
                meta("Primer paso", "Registra tu primer intento", d.intentos(), 1),
                meta("Constante", "Registra 10 intentos", d.intentos(), 10),
                meta("Veterano", "Registra 50 intentos", d.intentos(), 50),
                meta("En racha", "Consigue 5 aciertos seguidos", d.rachaMaxima(), 5),
                meta("Imparable", "Consigue 10 aciertos seguidos", d.rachaMaxima(), 10),
                new Logro("Francotirador", "80 % de acierto con al menos 10 intentos calificados",
                        calificados < 10 ? 0.5 * calificados / 10.0 : Math.min(1.0, precision / 0.8) * (precision >= 0.8 ? 1 : 0.95),
                        calificados < 10 ? calificados + " de 10 intentos calificados"
                                : String.format("Acierto actual: %.0f %%", precision * 100)),
                meta("Especialista", "Domina 1 algoritmo (5 intentos con 80 % de acierto)", d.algoritmosDominados(), 1),
                meta("Polivalente", "Domina 3 algoritmos", d.algoritmosDominados(), 3),
                meta("Maestro", "Domina 6 algoritmos", d.algoritmosDominados(), 6),
                meta("Corredor", "100 puntos en el desafío cronometrado", d.mejorDesafio(), 100),
                meta("Relámpago", "200 puntos en el desafío cronometrado", d.mejorDesafio(), 200),
                meta("Teórico", "Responde bien 10 de 10 en el cuestionario", d.mejorCuestionario(), 10));
    }

    private static Logro meta(String titulo, String descripcion, long actual, long objetivo) {
        return new Logro(titulo, descripcion, Math.min(1.0, (double) actual / objetivo),
                Math.min(actual, objetivo) + " de " + objetivo);
    }

    /** Racha más larga de aciertos; los intentos llegan del más nuevo al más antiguo y los no calificados se ignoran. */
    public static int rachaMaxima(List<EstadisticasDAO.IntentoReciente> recientes) {
        int mejor = 0;
        int actual = 0;
        for (EstadisticasDAO.IntentoReciente intento : recientes) {
            if (intento.correcto() == null) {
                continue;
            }
            actual = intento.correcto() ? actual + 1 : 0;
            mejor = Math.max(mejor, actual);
        }
        return mejor;
    }

    /** Algoritmos con al menos 5 intentos calificados y 80 % de acierto o más. */
    public static int algoritmosDominados(List<EstadisticasDAO.RendimientoAlgoritmo> rendimiento) {
        return (int) rendimiento.stream()
                .filter(r -> r.correctos() + r.incorrectos() >= 5 && r.porcentajeAciertos() >= 80.0)
                .count();
    }
}
