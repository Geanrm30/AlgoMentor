package ni.edu.uam.algomentor.util;

import java.util.Set;

public final class ValidacionesFormulario {

    private static final Set<String> CATEGORIAS = Set.of("ORDENAMIENTO", "MATEMATICO");
    private static final Set<String> ORIGENES = Set.of("ALEATORIO", "MANUAL", "PERSONALIZADO");

    private ValidacionesFormulario() {
    }

    public static void validarAlgoritmo(String nombre, String categoria, String complejidad,
                                        String descripcion) {
        validarTextoObligatorio(nombre, 80, "El nombre del algoritmo");
        if (!CATEGORIAS.contains(categoria)) {
            throw new IllegalArgumentException("Seleccioná una categoría válida");
        }
        validarTextoOpcional(complejidad, 30, "La complejidad");
        validarTextoOpcional(descripcion, 500, "La descripción");
    }

    public static void validarEjercicio(Object algoritmo, String origen, Integer dificultad,
                                        String datos) {
        if (algoritmo == null) {
            throw new IllegalArgumentException("Seleccioná un algoritmo");
        }
        if (!ORIGENES.contains(origen)) {
            throw new IllegalArgumentException("Seleccioná un origen válido");
        }
        if (dificultad == null || dificultad < 1 || dificultad > 5) {
            throw new IllegalArgumentException("La dificultad debe estar entre 1 y 5");
        }
        if (datos == null || datos.isBlank()) {
            throw new IllegalArgumentException("Ingresá los datos del ejercicio");
        }
    }

    private static void validarTextoObligatorio(String valor, int longitudMaxima, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }
        if (valor.trim().length() > longitudMaxima) {
            throw new IllegalArgumentException(campo + " supera " + longitudMaxima + " caracteres");
        }
    }

    private static void validarTextoOpcional(String valor, int longitudMaxima, String campo) {
        if (valor != null && valor.trim().length() > longitudMaxima) {
            throw new IllegalArgumentException(campo + " supera " + longitudMaxima + " caracteres");
        }
    }
}
