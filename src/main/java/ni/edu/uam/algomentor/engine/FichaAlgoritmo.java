package ni.edu.uam.algomentor.engine;

import java.util.List;
import java.util.Optional;

/** Descripción didáctica de un algoritmo de ordenamiento: cómo ordena y cómo se compara con los demás. */
public record FichaAlgoritmo(CatalogoAlgoritmos algoritmo, String tecnica, String mejor, String promedio,
                             String peor, String espacio, boolean estable, boolean enSitio,
                             String mecanica, String cuandoUsarlo) {

    private static final List<FichaAlgoritmo> FICHAS = List.of(
            new FichaAlgoritmo(CatalogoAlgoritmos.BUBBLE, "Intercambio de vecinos",
                    "O(n)", "O(n²)", "O(n²)", "O(1)", true, true,
                    "Recorre el arreglo comparando cada par de vecinos y los intercambia si están al revés. "
                            + "Tras cada pasada el mayor restante queda fijo al final, como una burbuja que sube. "
                            + "Si una pasada no intercambia nada, termina antes.",
                    "Para aprender: es el más simple. En la práctica casi nunca se usa con datos grandes."),
            new FichaAlgoritmo(CatalogoAlgoritmos.INSERTION, "Inserción",
                    "O(n)", "O(n²)", "O(n²)", "O(1)", true, true,
                    "Mantiene un tramo izquierdo ya ordenado. Toma el siguiente elemento y lo desplaza hacia la "
                            + "izquierda hasta encontrar su lugar, como al ordenar cartas en la mano.",
                    "Arreglos pequeños o casi ordenados; es muy rápido en esos casos."),
            new FichaAlgoritmo(CatalogoAlgoritmos.SELECTION, "Selección",
                    "O(n²)", "O(n²)", "O(n²)", "O(1)", false, true,
                    "En cada pasada busca el mínimo del tramo sin ordenar y lo intercambia con la primera posición "
                            + "de ese tramo. Hace pocos intercambios (máximo n-1), pero siempre compara todo.",
                    "Cuando escribir en memoria es costoso y n es pequeño."),
            new FichaAlgoritmo(CatalogoAlgoritmos.SHELL, "Inserción con saltos",
                    "O(n log n)", "≈ O(n^1.5)", "O(n²)", "O(1)", false, true,
                    "Aplica inserción sobre elementos separados por un salto grande (n/2) y lo va reduciendo hasta 1. "
                            + "Así los valores lejanos viajan rápido y la última pasada, con salto 1, encuentra el "
                            + "arreglo casi ordenado.",
                    "Tamaños medianos sin memoria extra; fácil de implementar."),
            new FichaAlgoritmo(CatalogoAlgoritmos.QUICK, "Divide y vencerás (partición)",
                    "O(n log n)", "O(n log n)", "O(n²)", "O(log n)", false, true,
                    "Elige un pivote y reparte los elementos en menores, iguales y mayores. Después repite el proceso "
                            + "en los subarreglos de los lados. El pivote queda en su posición final.",
                    "Opción general muy rápida en la práctica; el peor caso aparece con pivotes mal elegidos."),
            new FichaAlgoritmo(CatalogoAlgoritmos.MERGE, "Divide y vencerás (mezcla)",
                    "O(n log n)", "O(n log n)", "O(n log n)", "O(n)", true, false,
                    "Divide el arreglo por la mitad hasta tener un solo elemento y luego mezcla las mitades "
                            + "ordenadas comparando sus frentes. Necesita un arreglo auxiliar para mezclar.",
                    "Cuando se necesita tiempo garantizado y estabilidad, o con datos en disco."),
            new FichaAlgoritmo(CatalogoAlgoritmos.HEAP, "Montículo (heap)",
                    "O(n log n)", "O(n log n)", "O(n log n)", "O(1)", false, true,
                    "Convierte el arreglo en un montículo máximo (cada padre es mayor que sus hijos). Luego intercambia "
                            + "la raíz, que es el mayor, con el último elemento, reduce el montículo y lo repara.",
                    "Cuando se quiere O(n log n) garantizado sin memoria extra."),
            new FichaAlgoritmo(CatalogoAlgoritmos.COCKTAIL, "Intercambio bidireccional",
                    "O(n)", "O(n²)", "O(n²)", "O(1)", true, true,
                    "Variante de burbuja que alterna el sentido: una pasada sube el mayor al final y la siguiente "
                            + "baja el menor al inicio, así los valores pequeños del final no tardan tanto.",
                    "Mejora la burbuja cuando hay valores pequeños al final; sigue siendo didáctico."),
            new FichaAlgoritmo(CatalogoAlgoritmos.COMB, "Intercambio con brecha",
                    "O(n log n)", "O(n²/2^p)", "O(n²)", "O(1)", false, true,
                    "Como la burbuja, pero compara elementos separados por una brecha que empieza en n y se divide "
                            + "entre 1.3 en cada pasada hasta llegar a 1. Mueve rápido los valores pequeños del final.",
                    "Alternativa sencilla a burbuja, mucho más rápida en la práctica."),
            new FichaAlgoritmo(CatalogoAlgoritmos.GNOME, "Intercambio con retroceso",
                    "O(n)", "O(n²)", "O(n²)", "O(1)", true, true,
                    "Un gnomo recorre la fila: si el par está en orden avanza; si no, los intercambia y da un paso "
                            + "atrás. Es una inserción disfrazada, sin bucles anidados.",
                    "Solo para aprender: muestra una idea de ordenamiento con una sola posición activa."),
            new FichaAlgoritmo(CatalogoAlgoritmos.COUNTING, "Conteo (sin comparaciones)",
                    "O(n+k)", "O(n+k)", "O(n+k)", "O(k)", true, false,
                    "No compara elementos: cuenta cuántas veces aparece cada valor y reescribe el arreglo en orden "
                            + "según esas frecuencias. Funciona con enteros en un rango k no muy grande.",
                    "Enteros con rango pequeño; con rangos enormes pierde la ventaja."),
            new FichaAlgoritmo(CatalogoAlgoritmos.RADIX, "Dígito a dígito (sin comparaciones)",
                    "O(d(n+b))", "O(d(n+b))", "O(d(n+b))", "O(n+b)", true, false,
                    "Ordena por cada dígito, del menos al más significativo, con una pasada estable por dígito. "
                            + "Al terminar la última pasada el arreglo queda ordenado.",
                    "Muchos enteros con pocos dígitos (d pequeño)."),
            new FichaAlgoritmo(CatalogoAlgoritmos.BUCKET, "Distribución en cubetas",
                    "O(n+k)", "O(n+k)", "O(n²)", "O(n+k)", true, false,
                    "Reparte los valores en cubetas por intervalos, ordena cada cubeta por inserción y concatena "
                            + "las cubetas en orden. Si todo cae en una sola cubeta, degenera en O(n²).",
                    "Datos repartidos de forma uniforme dentro de un rango."));

    public static List<FichaAlgoritmo> todas() {
        return FICHAS;
    }

    public static Optional<FichaAlgoritmo> de(CatalogoAlgoritmos algoritmo) {
        return FICHAS.stream().filter(f -> f.algoritmo() == algoritmo).findFirst();
    }

    public String paraEstable() {
        return estable ? "Sí" : "No";
    }

    public String paraEnSitio() {
        return enSitio ? "Sí" : "No";
    }
}
