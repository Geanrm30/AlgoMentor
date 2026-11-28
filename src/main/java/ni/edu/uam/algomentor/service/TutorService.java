package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.engine.FichaAlgoritmo;
import ni.edu.uam.algomentor.engine.FormatoDatos;
import ni.edu.uam.algomentor.engine.Paso;
import ni.edu.uam.algomentor.engine.TipoOperacion;

import java.util.ArrayList;
import java.util.List;

/** Pistas escalonadas para un ejercicio de práctica: orientan sin revelar la respuesta final. */
public final class TutorService {

    public static final int MAX_PISTAS = 3;

    private TutorService() { }

    /** Hasta tres pistas, de la más general a la más concreta. Nunca incluyen el resultado final. */
    public static List<String> pistas(String nombreAlgoritmo, String datos) {
        CatalogoAlgoritmos algoritmo = CatalogoAlgoritmos.porNombre(nombreAlgoritmo);
        List<String> pistas = new ArrayList<>();
        if (algoritmo.esOrdenamiento()) {
            pistas.add("Pista 1: este algoritmo usa la técnica «" + FichaAlgoritmo.de(algoritmo).orElseThrow().tecnica()
                    + "». Simula un arreglo pequeño en Aprender > Simular y sigue el pseudocódigo.");
            List<Paso> cambios = algoritmo.crearOrdenamiento().ejecutar(FormatoDatos.leerArreglo(datos)).pasos().stream()
                    .filter(p -> p.tipo() == TipoOperacion.INTERCAMBIAR || p.tipo() == TipoOperacion.ESCRIBIR)
                    .toList();
            if (cambios.isEmpty()) {
                pistas.add("Pista 2: el arreglo ya está ordenado; este algoritmo no necesita hacer cambios.");
                pistas.add("Pista 3: escribe el mismo arreglo, sin modificarlo.");
            } else {
                pistas.add("Pista 2: el primer cambio del algoritmo es: " + cambios.get(0).explicacion());
                if (cambios.size() > 3) {
                    pistas.add("Pista 3: después de los primeros 3 cambios el arreglo queda " + cambios.get(2).estadoPosterior()
                            + ". Sigue desde ahí hasta que no queden cambios.");
                } else {
                    pistas.add("Pista 3: este ejercicio necesita muy pocos cambios (" + cambios.size()
                            + "). Aplícalos uno por uno y comprueba que el resultado quede de menor a mayor.");
                }
            }
        } else {
            pistas.add("Pista 1: busca el pivote de la primera columna; si vale 0, intercambia esa fila con otra que no lo tenga.");
            pistas.add("Pista 2: para anular un valor, resta a su fila un múltiplo de la fila del pivote: "
                    + "F(i) = F(i) − (a(i,k) / a(k,k)) · F(k). Cuida el signo del factor.");
            var pasos = algoritmo.crearMatricial().ejecutar(
                    ni.edu.uam.algomentor.util.EntradaAprendizaje.leerMatrizGuardada(datos)).ejecucion().pasos();
            pistas.add("Pista 3: el primer paso del método es: " + pasos.get(0).explicacion());
        }
        return pistas;
    }

    /** Consejo según el tipo de error que el perfil comete con más frecuencia. */
    public static String consejoPorError(String tipo) {
        if (tipo == null) {
            return "";
        }
        return switch (tipo) {
            case "SIGNO" -> "Según tu historial, fallas más en los signos: revisa cada resta y cada número negativo antes de escribir.";
            case "FRACCION" -> "Según tu historial, fallas más con fracciones: busca un denominador común y simplifica al final.";
            case "ARITMETICO" -> "Según tu historial, fallas más en el cálculo: repite cada operación una segunda vez.";
            case "OPERACION_INVALIDA" -> "Según tu historial, a veces eliges una operación que no corresponde: relee qué pide el ejercicio.";
            default -> "";
        };
    }
}
