package ni.edu.uam.algomentor.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Algoritmos de búsqueda, grafos y recursividad, con sus pasos explicados para la interfaz. */
public enum AlgoritmosExtra {
    BUSQUEDA_LINEAL("Búsqueda lineal", "Revisa los elementos uno por uno, de izquierda a derecha, hasta encontrar el objetivo.",
            "7, 3, 9, 1, 5 ; 9", "Arreglo, punto y coma y el valor a buscar. Ej. 7, 3, 9, 1, 5 ; 9",
            "O(n)", "Funciona con cualquier arreglo, ordenado o no."),
    BUSQUEDA_BINARIA("Búsqueda binaria", "Compara con el elemento del medio y descarta la mitad donde no puede estar el objetivo.",
            "1, 3, 5, 7, 9, 11, 13 ; 11", "Arreglo, punto y coma y el valor a buscar. Si no está ordenado, se ordena antes.",
            "O(log n)", "Necesita un arreglo ordenado; descarta la mitad en cada paso."),
    BFS("Recorrido en anchura (BFS)", "Visita primero todos los vecinos del nodo actual y luego los vecinos de los vecinos, usando una cola.",
            "A", "Nodo inicial, de la A a la F.", "O(V + E)", "Encuentra el camino con menos aristas en grafos sin pesos."),
    DFS("Recorrido en profundidad (DFS)", "Avanza por un camino hasta el final y retrocede cuando no hay más nodos nuevos, como una pila.",
            "A", "Nodo inicial, de la A a la F.", "O(V + E)", "Detectar ciclos, componentes conexas y recorrer laberintos."),
    DIJKSTRA("Camino más corto (Dijkstra)", "Fija en cada paso el nodo no visitado más cercano y mejora las distancias de sus vecinos.",
            "A", "Nodo inicial, de la A a la F. Los números en las aristas son sus pesos.",
            "O(V²) o O((V + E) log V)", "Distancias mínimas con pesos no negativos."),
    FIBONACCI("Fibonacci con memorización", "Guarda los valores ya calculados para no repetir trabajo: F(n) = F(n-1) + F(n-2).",
            "10", "Valor de n entre 0 y 30.", "O(n)", "Sin memorización, la versión recursiva hace un número exponencial de llamadas."),
    HANOI("Torres de Hanoi", "Mover n discos de la varilla A a la C sin poner un disco grande sobre uno pequeño, usando recursividad.",
            "3", "Cantidad de discos, de 1 a 6.", "O(2ⁿ)", "El ejemplo clásico de recursividad: se necesitan 2ⁿ − 1 movimientos.");

    private static final int MAX_ELEMENTOS = 20;

    private final String etiqueta;
    private final String descripcion;
    private final String entradaEjemplo;
    private final String ayudaEntrada;
    private final String complejidad;
    private final String cuandoUsarlo;

    AlgoritmosExtra(String etiqueta, String descripcion, String entradaEjemplo, String ayudaEntrada,
                    String complejidad, String cuandoUsarlo) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
        this.entradaEjemplo = entradaEjemplo;
        this.ayudaEntrada = ayudaEntrada;
        this.complejidad = complejidad;
        this.cuandoUsarlo = cuandoUsarlo;
    }

    public String etiqueta() { return etiqueta; }
    public String descripcion() { return descripcion; }
    public String entradaEjemplo() { return entradaEjemplo; }
    public String ayudaEntrada() { return ayudaEntrada; }
    public String complejidad() { return complejidad; }
    public String cuandoUsarlo() { return cuandoUsarlo; }

    public ResultadoExtra ejecutar(String entrada) {
        if (entrada == null || entrada.isBlank()) {
            throw new IllegalArgumentException("Escribe una entrada. " + ayudaEntrada);
        }
        return switch (this) {
            case BUSQUEDA_LINEAL -> busquedaLineal(entrada);
            case BUSQUEDA_BINARIA -> busquedaBinaria(entrada);
            case BFS -> anchura(nodoInicial(entrada));
            case DFS -> profundidad(nodoInicial(entrada));
            case DIJKSTRA -> dijkstra(nodoInicial(entrada));
            case FIBONACCI -> fibonacci(entero(entrada, 0, 30, "n"));
            case HANOI -> hanoi(entero(entrada, 1, 6, "El número de discos"));
        };
    }

    // ---------- Entradas ----------

    private static int entero(String texto, int minimo, int maximo, String nombre) {
        try {
            int valor = Integer.parseInt(texto.strip());
            if (valor < minimo || valor > maximo) {
                throw new IllegalArgumentException(nombre + " debe estar entre " + minimo + " y " + maximo + ".");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nombre + " debe ser un número entero entre " + minimo + " y " + maximo + ".");
        }
    }

    private static String nodoInicial(String texto) {
        String nodo = texto.strip().toUpperCase(java.util.Locale.ROOT);
        if (!GrafoEjemplo.NODOS.contains(nodo)) {
            throw new IllegalArgumentException("El nodo inicial debe ser una letra de la A a la F.");
        }
        return nodo;
    }

    private record ArregloYObjetivo(int[] datos, int objetivo) { }

    private static ArregloYObjetivo arregloYObjetivo(String entrada) {
        String[] partes = entrada.split(";", -1);
        if (partes.length != 2) {
            throw new IllegalArgumentException("Usa el formato: arreglo ; objetivo. Ej. 7, 3, 9, 1, 5 ; 9");
        }
        String[] numeros = partes[0].strip().split("\\s*,\\s*");
        if (partes[0].isBlank() || numeros.length > MAX_ELEMENTOS) {
            throw new IllegalArgumentException("El arreglo debe tener entre 1 y " + MAX_ELEMENTOS + " números.");
        }
        try {
            int[] datos = Arrays.stream(numeros).mapToInt(Integer::parseInt).toArray();
            return new ArregloYObjetivo(datos, Integer.parseInt(partes[1].strip()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Solo se admiten números enteros.");
        }
    }

    private static List<String> texto(int[] datos) {
        return Arrays.stream(datos).mapToObj(Integer::toString).toList();
    }

    // ---------- Búsquedas ----------

    private static ResultadoExtra busquedaLineal(String entrada) {
        ArregloYObjetivo e = arregloYObjetivo(entrada);
        List<PasoExtra> pasos = new ArrayList<>();
        List<String> valores = texto(e.datos());
        Set<Integer> descartados = new TreeSet<>();
        for (int i = 0; i < e.datos().length; i++) {
            boolean igual = e.datos()[i] == e.objetivo();
            pasos.add(new PasoExtra(pasos.size() + 1, "Comparar " + e.datos()[i] + " (posición " + (i + 1) + ") con "
                    + e.objetivo() + ": " + (igual ? "son iguales, ¡encontrado!" : "son distintos, se sigue."),
                    new EstadoVisual.Celdas(valores, Set.of(i), igual ? Set.of(i) : Set.of(), Set.copyOf(descartados))));
            if (igual) {
                return new ResultadoExtra(pasos, "Encontrado en la posición " + (i + 1) + " tras " + (i + 1) + " comparaciones.");
            }
            descartados.add(i);
        }
        pasos.add(new PasoExtra(pasos.size() + 1, "Se revisaron todos los elementos y ninguno es " + e.objetivo() + ".",
                new EstadoVisual.Celdas(valores, Set.of(), Set.of(), Set.copyOf(descartados))));
        return new ResultadoExtra(pasos, "No está en el arreglo (" + e.datos().length + " comparaciones).");
    }

    private static ResultadoExtra busquedaBinaria(String entrada) {
        ArregloYObjetivo e = arregloYObjetivo(entrada);
        int[] a = e.datos().clone();
        List<PasoExtra> pasos = new ArrayList<>();
        int[] ordenado = a.clone();
        Arrays.sort(ordenado);
        if (!Arrays.equals(a, ordenado)) {
            a = ordenado;
            pasos.add(new PasoExtra(1, "El arreglo no estaba ordenado y la búsqueda binaria lo necesita: se ordena primero.",
                    new EstadoVisual.Celdas(texto(a), Set.of(), Set.of(), Set.of())));
        }
        List<String> valores = texto(a);
        int bajo = 0;
        int alto = a.length - 1;
        int comparaciones = 0;
        while (bajo <= alto) {
            int medio = (bajo + alto) >>> 1;
            comparaciones++;
            Set<Integer> descartados = new TreeSet<>();
            for (int i = 0; i < a.length; i++) if (i < bajo || i > alto) descartados.add(i);
            String rango = "tramo [" + (bajo + 1) + ", " + (alto + 1) + "]";
            if (a[medio] == e.objetivo()) {
                pasos.add(new PasoExtra(pasos.size() + 1, "En el " + rango + " el medio es la posición " + (medio + 1) + " con valor "
                        + a[medio] + ": es el objetivo, ¡encontrado!",
                        new EstadoVisual.Celdas(valores, Set.of(medio), Set.of(medio), descartados)));
                return new ResultadoExtra(pasos, "Encontrado en la posición " + (medio + 1) + " (del arreglo ordenado) tras "
                        + comparaciones + " comparaciones.");
            }
            boolean menor = a[medio] < e.objetivo();
            pasos.add(new PasoExtra(pasos.size() + 1, "En el " + rango + " el medio es la posición " + (medio + 1) + " con valor "
                    + a[medio] + ": " + a[medio] + (menor ? " < " : " > ") + e.objetivo() + ", se descarta la mitad "
                    + (menor ? "izquierda" : "derecha") + ".",
                    new EstadoVisual.Celdas(valores, Set.of(medio), Set.of(), descartados)));
            if (menor) bajo = medio + 1;
            else alto = medio - 1;
        }
        Set<Integer> todos = new TreeSet<>();
        for (int i = 0; i < a.length; i++) todos.add(i);
        pasos.add(new PasoExtra(pasos.size() + 1, "El tramo quedó vacío: el objetivo no está.",
                new EstadoVisual.Celdas(valores, Set.of(), Set.of(), todos)));
        return new ResultadoExtra(pasos, "No está en el arreglo (" + comparaciones + " comparaciones).");
    }

    // ---------- Grafos ----------

    private static EstadoVisual.Grafo grafo(Map<String, String> estados, Map<String, String> etiquetas,
                                            Set<String> aristas, String cola) {
        return new EstadoVisual.Grafo(new LinkedHashMap<>(estados), new LinkedHashMap<>(etiquetas), Set.copyOf(aristas), cola);
    }

    private static ResultadoExtra anchura(String inicio) {
        List<PasoExtra> pasos = new ArrayList<>();
        Map<String, String> estados = new LinkedHashMap<>();
        Map<String, String> orden = new LinkedHashMap<>();
        Deque<String> cola = new ArrayDeque<>();
        List<String> visitados = new ArrayList<>();
        estados.put(inicio, "frontera");
        cola.add(inicio);
        pasos.add(new PasoExtra(1, "Se coloca " + inicio + " en la cola.", grafo(estados, orden, Set.of(), cola.toString())));
        while (!cola.isEmpty()) {
            String actual = cola.poll();
            estados.put(actual, "actual");
            visitados.add(actual);
            orden.put(actual, Integer.toString(visitados.size()));
            pasos.add(new PasoExtra(pasos.size() + 1, "Se saca " + actual + " de la cola y se visita (lugar " + visitados.size() + ").",
                    grafo(estados, orden, Set.of(), cola.toString())));
            for (GrafoEjemplo.Arista vecino : GrafoEjemplo.vecinos(actual)) {
                if (!estados.containsKey(vecino.b())) {
                    estados.put(vecino.b(), "frontera");
                    cola.add(vecino.b());
                    pasos.add(new PasoExtra(pasos.size() + 1, "Desde " + actual + " se descubre " + vecino.b() + " y entra a la cola.",
                            grafo(estados, orden, Set.of(vecino.clave()), cola.toString())));
                }
            }
            estados.put(actual, "visitado");
        }
        pasos.add(new PasoExtra(pasos.size() + 1, "La cola quedó vacía: se visitaron todos los nodos alcanzables.",
                grafo(estados, orden, Set.of(), cola.toString())));
        return new ResultadoExtra(pasos, "Orden de visita: " + String.join(", ", visitados) + ".");
    }

    private static ResultadoExtra profundidad(String inicio) {
        List<PasoExtra> pasos = new ArrayList<>();
        Map<String, String> estados = new LinkedHashMap<>();
        Map<String, String> orden = new LinkedHashMap<>();
        List<String> visitados = new ArrayList<>();
        Deque<String> pila = new ArrayDeque<>();
        visitar(inicio, null, estados, orden, visitados, pila, pasos);
        pasos.add(new PasoExtra(pasos.size() + 1, "Se retrocedió hasta el inicio: no quedan nodos nuevos.",
                grafo(estados, orden, Set.of(), pila.toString())));
        return new ResultadoExtra(pasos, "Orden de visita: " + String.join(", ", visitados) + ".");
    }

    private static void visitar(String nodo, GrafoEjemplo.Arista llegada, Map<String, String> estados,
                                Map<String, String> orden, List<String> visitados, Deque<String> pila, List<PasoExtra> pasos) {
        estados.put(nodo, "actual");
        visitados.add(nodo);
        pila.push(nodo);
        orden.put(nodo, Integer.toString(visitados.size()));
        pasos.add(new PasoExtra(pasos.size() + 1, (llegada == null ? "Se empieza en " + nodo : "Se avanza a " + nodo)
                + " (lugar " + visitados.size() + ").",
                grafo(estados, orden, llegada == null ? Set.of() : Set.of(llegada.clave()), pila.toString())));
        for (GrafoEjemplo.Arista vecino : GrafoEjemplo.vecinos(nodo)) {
            if (!estados.containsKey(vecino.b())) {
                visitar(vecino.b(), vecino, estados, orden, visitados, pila, pasos);
                estados.put(nodo, "actual");
                pasos.add(new PasoExtra(pasos.size() + 1, "Se retrocede a " + nodo + " para probar otros vecinos.",
                        grafo(estados, orden, Set.of(vecino.clave()), pila.toString())));
            }
        }
        estados.put(nodo, "visitado");
        pila.pop();
    }

    private static ResultadoExtra dijkstra(String inicio) {
        List<PasoExtra> pasos = new ArrayList<>();
        Map<String, Integer> distancia = new LinkedHashMap<>();
        for (String nodo : GrafoEjemplo.NODOS) distancia.put(nodo, Integer.MAX_VALUE);
        distancia.put(inicio, 0);
        Set<String> fijos = new HashSet<>();
        Map<String, String> estados = new LinkedHashMap<>();
        estados.put(inicio, "frontera");
        pasos.add(new PasoExtra(1, "La distancia a " + inicio + " es 0 y las demás son infinitas.",
                grafo(estados, etiquetas(distancia), Set.of(), "")));
        while (true) {
            String actual = null;
            for (String nodo : GrafoEjemplo.NODOS) {
                if (!fijos.contains(nodo) && distancia.get(nodo) != Integer.MAX_VALUE
                        && (actual == null || distancia.get(nodo) < distancia.get(actual))) {
                    actual = nodo;
                }
            }
            if (actual == null) break;
            fijos.add(actual);
            estados.put(actual, "actual");
            pasos.add(new PasoExtra(pasos.size() + 1, "Se fija " + actual + ": es el nodo más cercano sin fijar, a distancia "
                    + distancia.get(actual) + ".", grafo(estados, etiquetas(distancia), Set.of(), "")));
            for (GrafoEjemplo.Arista vecino : GrafoEjemplo.vecinos(actual)) {
                if (fijos.contains(vecino.b())) continue;
                int candidata = distancia.get(actual) + vecino.peso();
                boolean mejora = candidata < distancia.get(vecino.b());
                String antes = distancia.get(vecino.b()) == Integer.MAX_VALUE ? "∞" : distancia.get(vecino.b()).toString();
                if (mejora) {
                    distancia.put(vecino.b(), candidata);
                    estados.put(vecino.b(), "frontera");
                }
                pasos.add(new PasoExtra(pasos.size() + 1, "Camino por " + actual + " hasta " + vecino.b() + ": "
                        + (candidata - vecino.peso()) + " + " + vecino.peso() + " = " + candidata
                        + (mejora ? " < " + antes + ", se actualiza." : " >= " + antes + ", se conserva."),
                        grafo(estados, etiquetas(distancia), Set.of(vecino.clave()), "")));
            }
            estados.put(actual, "visitado");
        }
        pasos.add(new PasoExtra(pasos.size() + 1, "Todos los nodos alcanzables están fijos.",
                grafo(estados, etiquetas(distancia), Set.of(), "")));
        StringBuilder resumen = new StringBuilder("Distancias desde " + inicio + ": ");
        for (String nodo : GrafoEjemplo.NODOS) {
            resumen.append(nodo).append('=').append(distancia.get(nodo) == Integer.MAX_VALUE ? "∞" : distancia.get(nodo)).append(", ");
        }
        return new ResultadoExtra(pasos, resumen.substring(0, resumen.length() - 2) + ".");
    }

    private static Map<String, String> etiquetas(Map<String, Integer> distancia) {
        Map<String, String> etiquetas = new LinkedHashMap<>();
        distancia.forEach((nodo, d) -> etiquetas.put(nodo, d == Integer.MAX_VALUE ? "∞" : d.toString()));
        return etiquetas;
    }

    // ---------- Recursividad ----------

    private static ResultadoExtra fibonacci(int n) {
        List<PasoExtra> pasos = new ArrayList<>();
        long[] f = new long[Math.max(2, n + 1)];
        f[1] = 1;
        List<String> mostrados = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            if (i >= 2) f[i] = f[i - 1] + f[i - 2];
            mostrados.add(Long.toString(f[i]));
            String explicacion = i < 2 ? "Caso base: F(" + i + ") = " + f[i] + "."
                    : "F(" + i + ") = F(" + (i - 1) + ") + F(" + (i - 2) + ") = " + f[i - 1] + " + " + f[i - 2] + " = " + f[i] + ".";
            Set<Integer> activos = i < 2 ? Set.of(i) : Set.of(i, i - 1, i - 2);
            pasos.add(new PasoExtra(pasos.size() + 1, explicacion,
                    new EstadoVisual.Celdas(List.copyOf(mostrados), activos, Set.of(i), Set.of())));
        }
        long[] llamadas = new long[Math.max(2, n + 1)];
        llamadas[0] = 1;
        llamadas[1] = 1;
        for (int i = 2; i <= n; i++) llamadas[i] = llamadas[i - 1] + llamadas[i - 2] + 1;
        return new ResultadoExtra(pasos, "F(" + n + ") = " + f[n] + ". Con memorización son " + (n + 1)
                + " cálculos; la recursión sin memoria haría " + llamadas[n] + " llamadas.");
    }

    private static ResultadoExtra hanoi(int discos) {
        List<PasoExtra> pasos = new ArrayList<>();
        List<Deque<Integer>> varillas = List.of(new ArrayDeque<>(), new ArrayDeque<>(), new ArrayDeque<>());
        for (int d = discos; d >= 1; d--) varillas.get(0).push(d);
        pasos.add(new PasoExtra(1, "Los " + discos + " discos empiezan en la varilla A.", estadoTorres(varillas, discos)));
        mover(discos, 0, 2, 1, varillas, pasos, discos);
        return new ResultadoExtra(pasos, "Se resolvió en " + (pasos.size() - 1) + " movimientos (2^" + discos + " − 1 = "
                + ((1 << discos) - 1) + ").");
    }

    private static void mover(int n, int origen, int destino, int auxiliar, List<Deque<Integer>> varillas,
                              List<PasoExtra> pasos, int total) {
        if (n == 0) return;
        mover(n - 1, origen, auxiliar, destino, varillas, pasos, total);
        int disco = varillas.get(origen).pop();
        varillas.get(destino).push(disco);
        String nombres = "ABC";
        pasos.add(new PasoExtra(pasos.size() + 1, "Mover el disco " + disco + " de la varilla " + nombres.charAt(origen)
                + " a la " + nombres.charAt(destino) + ".", estadoTorres(varillas, total)));
        mover(n - 1, auxiliar, destino, origen, varillas, pasos, total);
    }

    private static EstadoVisual.Torres estadoTorres(List<Deque<Integer>> varillas, int total) {
        List<List<Integer>> copia = new ArrayList<>();
        for (Deque<Integer> varilla : varillas) {
            List<Integer> discos = new ArrayList<>(varilla);
            java.util.Collections.reverse(discos);
            copia.add(discos);
        }
        return new EstadoVisual.Torres(copia, total);
    }
}
