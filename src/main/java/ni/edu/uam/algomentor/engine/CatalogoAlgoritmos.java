package ni.edu.uam.algomentor.engine;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/** Nombres persistidos, etiquetas y fábricas compartidos por interfaz, práctica y terminal. */
public enum CatalogoAlgoritmos {
    BUBBLE("Bubble Sort", "Ordenamiento burbuja", "bubble", BubbleSort::new, null),
    INSERTION("Insertion Sort", "Ordenamiento por inserción", "insertion", InsertionSort::new, null),
    QUICK("Quick Sort", "Quick Sort (rápido)", "quick", QuickSort::new, null),
    MERGE("Merge Sort", "Merge Sort (mezcla)", "merge", MergeSort::new, null),
    COUNTING("Counting Sort", "Counting Sort (conteo)", "counting", CountingSort::new, null),
    RADIX("Radix Sort", "Radix Sort (dígitos)", "radix", RadixSort::new, null),
    BUCKET("Bucket Sort", "Bucket Sort (cubetas)", "bucket", BucketSort::new, null),
    SELECTION("Selection Sort", "Ordenamiento por selección", "selection", SelectionSort::new, null),
    SHELL("Shell Sort", "Shell Sort (saltos)", "shell", ShellSort::new, null),
    HEAP("Heap Sort", "Heap Sort (montículo)", "heap", HeapSort::new, null),
    COCKTAIL("Cocktail Sort", "Cocktail Sort (bidireccional)", "cocktail", CocktailSort::new, null),
    COMB("Comb Sort", "Comb Sort (peine)", "comb", CombSort::new, null),
    GNOME("Gnome Sort", "Gnome Sort (gnomo)", "gnome", GnomeSort::new, null),
    GAUSS("Gauss", "Eliminación de Gauss", "gauss", null, Gauss::new),
    JORDAN("Gauss-Jordan", "Eliminación de Gauss-Jordan", "jordan", null, GaussJordan::new);

    private final String nombre;
    private final String etiqueta;
    private final String comando;
    private final Supplier<AlgoritmoOrdenamiento> ordenamiento;
    private final Supplier<AlgoritmoMatricial> matricial;

    CatalogoAlgoritmos(String nombre, String etiqueta, String comando,
                      Supplier<AlgoritmoOrdenamiento> ordenamiento, Supplier<AlgoritmoMatricial> matricial) {
        this.nombre = nombre;
        this.etiqueta = etiqueta;
        this.comando = comando;
        this.ordenamiento = ordenamiento;
        this.matricial = matricial;
    }

    public String nombre() { return nombre; }
    public String etiqueta() { return etiqueta; }
    public String comando() { return comando; }
    public boolean esOrdenamiento() { return ordenamiento != null; }

    public AlgoritmoOrdenamiento crearOrdenamiento() {
        if (!esOrdenamiento()) throw new IllegalArgumentException("Selecciona un algoritmo de ordenamiento.");
        return ordenamiento.get();
    }

    public AlgoritmoMatricial crearMatricial() {
        if (matricial == null) throw new IllegalArgumentException("Selecciona un método de sistemas lineales.");
        return matricial.get();
    }

    public static List<String> nombres() {
        return Arrays.stream(values()).map(CatalogoAlgoritmos::nombre).toList();
    }

    public static List<CatalogoAlgoritmos> ordenamientos() {
        return Arrays.stream(values()).filter(CatalogoAlgoritmos::esOrdenamiento).toList();
    }

    public static boolean admite(String nombre) {
        return Arrays.stream(values()).anyMatch(a -> a.nombre.equals(nombre));
    }

    public static CatalogoAlgoritmos porNombre(String nombre) {
        return Arrays.stream(values()).filter(a -> a.nombre.equals(nombre)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Algoritmo no disponible: " + nombre));
    }

    public static CatalogoAlgoritmos porComando(String comando) {
        return Arrays.stream(values()).filter(a -> a.comando.equalsIgnoreCase(comando)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Algoritmo desconocido. Usa ayuda para ver las opciones."));
    }
}
