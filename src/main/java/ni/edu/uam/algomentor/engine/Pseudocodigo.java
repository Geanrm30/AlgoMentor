package ni.edu.uam.algomentor.engine;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static ni.edu.uam.algomentor.engine.TipoOperacion.*;

/**
 * Pseudocódigo de cada algoritmo. Cada línea declara qué tipos de operación ejecuta, de modo que el
 * simulador pueda resaltar la línea que corresponde al paso que se está mostrando.
 */
public final class Pseudocodigo {

    private record Linea(String texto, Set<TipoOperacion> tipos) { }

    private final List<Linea> lineas;

    private Pseudocodigo(List<Linea> lineas) {
        this.lineas = lineas;
    }

    private static Linea l(String texto, TipoOperacion... tipos) {
        return new Linea(texto, tipos.length == 0 ? EnumSet.noneOf(TipoOperacion.class) : EnumSet.copyOf(Arrays.asList(tipos)));
    }

    private static Pseudocodigo de(Linea... lineas) {
        return new Pseudocodigo(List.of(lineas));
    }

    private static final Map<CatalogoAlgoritmos, Pseudocodigo> TODOS = Map.ofEntries(
            Map.entry(CatalogoAlgoritmos.BUBBLE, de(
                    l("para i desde 0 hasta n-2"),
                    l("    para j desde 0 hasta n-2-i"),
                    l("        si a[j] > a[j+1]", COMPARAR),
                    l("            intercambiar a[j] con a[j+1]", INTERCAMBIAR),
                    l("    si no hubo cambios: terminar"),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.INSERTION, de(
                    l("para i desde 1 hasta n-1"),
                    l("    j = i"),
                    l("    mientras j > 0 y a[j-1] > a[j]", COMPARAR),
                    l("        intercambiar a[j-1] con a[j]", INTERCAMBIAR),
                    l("        j = j - 1"),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.SELECTION, de(
                    l("para i desde 0 hasta n-2"),
                    l("    minimo = i"),
                    l("    para j desde i+1 hasta n-1"),
                    l("        si a[j] < a[minimo]: minimo = j", COMPARAR),
                    l("    intercambiar a[i] con a[minimo]", INTERCAMBIAR),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.SHELL, de(
                    l("salto = n / 2", PASADA),
                    l("mientras salto >= 1"),
                    l("    insertar con salto: para i desde salto hasta n-1"),
                    l("        mientras j >= salto y a[j-salto] > a[j]", COMPARAR),
                    l("            intercambiar a[j-salto] con a[j]", INTERCAMBIAR),
                    l("    salto = salto / 2"),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.QUICK, de(
                    l("quicksort(inicio, fin)"),
                    l("    pivote = elemento del medio", PIVOTE),
                    l("    para cada elemento x del tramo", COMPARAR),
                    l("        mover x a menores, iguales o mayores", INTERCAMBIAR),
                    l("    queda dividido en < pivote | = pivote | > pivote", PARTICIONAR),
                    l("    quicksort(menores); quicksort(mayores)"),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.MERGE, de(
                    l("mergesort(inicio, fin)"),
                    l("    si inicio >= fin: devolver"),
                    l("    medio = (inicio + fin) / 2; ordenar cada mitad", DIVIDIR),
                    l("    mezclar(inicio, medio, fin)", MEZCLAR),
                    l("        mientras haya elementos en ambas mitades", COMPARAR),
                    l("            escribir el menor de los dos frentes", ESCRIBIR),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.HEAP, de(
                    l("construir montículo máximo", PASADA),
                    l("    hundir(i) para i desde n/2-1 hasta 0"),
                    l("    si hijo > padre", COMPARAR),
                    l("        intercambiar hijo con padre", INTERCAMBIAR),
                    l("para fin desde n-1 hasta 1"),
                    l("    intercambiar a[0] con a[fin]; hundir(0)"),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.COCKTAIL, de(
                    l("repetir", PASADA),
                    l("    recorrer de izquierda a derecha y de derecha a izquierda"),
                    l("        si a[i] > a[i+1]", COMPARAR),
                    l("            intercambiar a[i] con a[i+1]", INTERCAMBIAR),
                    l("hasta que no haya cambios"),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.COMB, de(
                    l("brecha = n", PASADA),
                    l("mientras brecha > 1 o hubo cambios"),
                    l("    brecha = brecha / 1.3"),
                    l("    si a[i] > a[i+brecha]", COMPARAR),
                    l("        intercambiar a[i] con a[i+brecha]", INTERCAMBIAR),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.GNOME, de(
                    l("i = 1"),
                    l("mientras i < n"),
                    l("    si a[i-1] > a[i]", COMPARAR),
                    l("        intercambiar y retroceder: i = i - 1", INTERCAMBIAR),
                    l("    si no: i = i + 1"),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.COUNTING, de(
                    l("calcular minimo y maximo", PASADA),
                    l("para cada x en a: frecuencia[x]++", CONTAR),
                    l("recorrer los valores en orden", COMPARAR),
                    l("    escribir x tantas veces como su frecuencia", ESCRIBIR),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.RADIX, de(
                    l("para cada dígito, del menos al más significativo", PASADA),
                    l("    contar cuántos elementos tienen cada dígito", CONTAR),
                    l("    distribuir los elementos por ese dígito (estable)", DISTRIBUIR),
                    l("    escribir el resultado en a", ESCRIBIR),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.BUCKET, de(
                    l("crear n cubetas para el rango [minimo, maximo]", PASADA),
                    l("para cada x en a: ponerlo en su cubeta", DISTRIBUIR),
                    l("para cada cubeta: ordenarla por inserción", ORDENAR_CUBETA, COMPARAR, ESCRIBIR_AUXILIAR),
                    l("concatenar las cubetas en a", ESCRIBIR),
                    l("devolver a", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.GAUSS, de(
                    l("para cada columna c (pivote en la fila c)"),
                    l("    si el pivote es 0: intercambiar con otra fila", INTERCAMBIAR_FILAS),
                    l("    para cada fila debajo: fila = fila - factor * fila_pivote", ELIMINAR_FILA, ESCALAR_FILA),
                    l("sustitución hacia atrás: despejar xn, ..., x1", SUSTITUIR_ATRAS),
                    l("devolver la solución", FINALIZAR))),
            Map.entry(CatalogoAlgoritmos.JORDAN, de(
                    l("para cada columna c (pivote en la fila c)"),
                    l("    si el pivote es 0: intercambiar con otra fila", INTERCAMBIAR_FILAS),
                    l("    dividir la fila del pivote para que valga 1", ESCALAR_FILA),
                    l("    anular la columna en todas las demás filas", ELIMINAR_FILA),
                    l("leer la solución en la última columna", SUSTITUIR_ATRAS),
                    l("devolver la solución", FINALIZAR))));

    public static Optional<Pseudocodigo> de(CatalogoAlgoritmos algoritmo) {
        return Optional.ofNullable(TODOS.get(algoritmo));
    }

    public List<String> lineas() {
        return lineas.stream().map(Linea::texto).toList();
    }

    /** Índice de la primera línea que ejecuta ese tipo de operación, o -1 si ninguna lo declara. */
    public int lineaPara(TipoOperacion tipo) {
        if (tipo == null) {
            return -1;
        }
        for (int i = 0; i < lineas.size(); i++) {
            if (lineas.get(i).tipos().contains(tipo)) {
                return i;
            }
        }
        return -1;
    }
}
