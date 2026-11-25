package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.DificultadRealDAO.EstadisticaEjercicio;

import java.util.StringJoiner;

/** Reglas heurísticas provisionales y ajustables; no constituyen criterios científicos. */
final class CalibracionDificultad {

    static final int MIN_INTENTOS = 10;
    private static final int[] MIN_ACIERTO = {70, 55, 35, 15, 0};
    private static final int[] MAX_ACIERTO = {100, 85, 65, 45, 30};

    enum Diagnostico {
        BIEN_CALIBRADO("BIEN CALIBRADO"),
        MAL_CALIBRADO("MAL CALIBRADO"),
        DATOS_INSUFICIENTES("DATOS INSUFICIENTES");

        private final String etiqueta;

        Diagnostico(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        String etiqueta() {
            return etiqueta;
        }
    }

    private CalibracionDificultad() { }

    static Diagnostico clasificar(EstadisticaEjercicio ejercicio) {
        if (ejercicio.intentos() < MIN_INTENTOS) {
            return Diagnostico.DATOS_INSUFICIENTES;
        }
        int indice = ejercicio.dificultad() - 1;
        double porcentaje = ejercicio.porcentajeAciertos();
        // Comparar antes de redondear; los dos extremos del rango están incluidos.
        return porcentaje >= MIN_ACIERTO[indice] && porcentaje <= MAX_ACIERTO[indice]
                ? Diagnostico.BIEN_CALIBRADO : Diagnostico.MAL_CALIBRADO;
    }

    static String descripcionReglas() {
        StringJoiner rangos = new StringJoiner(" · ");
        for (int i = 0; i < MIN_ACIERTO.length; i++) {
            rangos.add("Nivel " + (i + 1) + ": " + MIN_ACIERTO[i] + "–" + MAX_ACIERTO[i] + "%");
        }
        return "Mínimo " + MIN_INTENTOS + " intentos evaluados por ejercicio. Rangos inclusivos: " + rangos
                + ". El diagnóstico usa el porcentaje sin redondear.";
    }
}
