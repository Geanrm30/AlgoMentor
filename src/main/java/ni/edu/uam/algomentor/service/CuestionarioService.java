package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.AlgoritmosExtra;
import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.engine.FichaAlgoritmo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/** Preguntas de opción múltiple sobre la teoría de los algoritmos, generadas a partir de las fichas. */
public final class CuestionarioService {

    public record Pregunta(String enunciado, List<String> opciones, int correcta, String explicacion) {
        public Pregunta {
            opciones = List.copyOf(opciones);
        }
    }

    private static final List<Function<Random, Pregunta>> GENERADORES = List.of(
            azar -> sobreFicha(azar, "peor caso", FichaAlgoritmo::peor),
            azar -> sobreFicha(azar, "mejor caso", FichaAlgoritmo::mejor),
            azar -> sobreFicha(azar, "memoria extra", FichaAlgoritmo::espacio),
            CuestionarioService::estabilidad,
            CuestionarioService::tecnica,
            CuestionarioService::noComparan,
            CuestionarioService::queAlgoritmoEs,
            CuestionarioService::complejidadExtra,
            CuestionarioService::estructuraDeRecorrido,
            CuestionarioService::movimientosDeHanoi);

    private CuestionarioService() { }

    /** Genera {@code cantidad} preguntas distintas (máximo 12). */
    public static List<Pregunta> generar(long semilla, int cantidad) {
        Random azar = new Random(semilla);
        List<Pregunta> preguntas = new ArrayList<>();
        Set<String> vistas = new LinkedHashSet<>();
        int intentos = 0;
        while (preguntas.size() < Math.min(cantidad, 12) && intentos++ < 400) {
            Pregunta p = GENERADORES.get(azar.nextInt(GENERADORES.size())).apply(azar);
            if (vistas.add(p.enunciado())) {
                preguntas.add(p);
            }
        }
        return preguntas;
    }

    private static FichaAlgoritmo ficha(Random azar) {
        List<FichaAlgoritmo> fichas = FichaAlgoritmo.todas();
        return fichas.get(azar.nextInt(fichas.size()));
    }

    /** Correcta + hasta tres distractores distintos tomados del conjunto, mezclados. */
    static Pregunta conOpciones(Random azar, String enunciado, String correcta, List<String> conjunto, String explicacion) {
        List<String> candidatos = new ArrayList<>(new LinkedHashSet<>(conjunto));
        candidatos.remove(correcta);
        Collections.shuffle(candidatos, azar);
        List<String> opciones = new ArrayList<>(candidatos.subList(0, Math.min(3, candidatos.size())));
        opciones.add(correcta);
        Collections.shuffle(opciones, azar);
        return new Pregunta(enunciado, opciones, opciones.indexOf(correcta), explicacion);
    }

    private static Pregunta sobreFicha(Random azar, String que, Function<FichaAlgoritmo, String> dato) {
        FichaAlgoritmo f = ficha(azar);
        List<String> conjunto = FichaAlgoritmo.todas().stream().map(dato).toList();
        String articulo = que.startsWith("memoria") ? "la" : "el";
        return conOpciones(azar, "¿Cuál es " + articulo + " " + que + " de " + f.algoritmo().nombre() + "?",
                dato.apply(f), conjunto,
                f.algoritmo().nombre() + ": " + dato.apply(f) + ". " + f.mecanica());
    }

    private static Pregunta estabilidad(Random azar) {
        FichaAlgoritmo f = ficha(azar);
        String correcta = f.estable() ? "Sí" : "No";
        return new Pregunta("¿" + f.algoritmo().nombre() + " es un ordenamiento estable?", List.of("Sí", "No"),
                f.estable() ? 0 : 1, "Estable significa que los valores repetidos conservan su orden relativo. "
                + f.algoritmo().nombre() + (f.estable() ? " lo es." : " no lo garantiza.") + " (" + correcta + ")");
    }

    private static Pregunta tecnica(Random azar) {
        FichaAlgoritmo f = ficha(azar);
        return conOpciones(azar, "¿Qué técnica usa " + f.algoritmo().nombre() + "?", f.tecnica(),
                FichaAlgoritmo.todas().stream().map(FichaAlgoritmo::tecnica).toList(), f.mecanica());
    }

    private static Pregunta noComparan(Random azar) {
        List<CatalogoAlgoritmos> sinComparar = List.of(CatalogoAlgoritmos.COUNTING, CatalogoAlgoritmos.RADIX);
        CatalogoAlgoritmos correcta = sinComparar.get(azar.nextInt(sinComparar.size()));
        List<String> conjunto = new ArrayList<>(CatalogoAlgoritmos.ordenamientos().stream()
                .filter(a -> !sinComparar.contains(a) && a != CatalogoAlgoritmos.BUCKET)
                .map(CatalogoAlgoritmos::nombre).toList());
        conjunto.add(correcta.nombre());
        return conOpciones(azar, "¿Cuál de estos algoritmos NO ordena comparando elementos entre sí?", correcta.nombre(),
                conjunto, FichaAlgoritmo.de(correcta).orElseThrow().mecanica());
    }

    private static Pregunta queAlgoritmoEs(Random azar) {
        FichaAlgoritmo f = ficha(azar);
        return conOpciones(azar, "¿Qué algoritmo funciona así? «" + f.mecanica() + "»", f.algoritmo().nombre(),
                FichaAlgoritmo.todas().stream().map(x -> x.algoritmo().nombre()).toList(),
                "Es " + f.algoritmo().nombre() + ": " + f.tecnica() + ".");
    }

    private static Pregunta complejidadExtra(Random azar) {
        List<AlgoritmosExtra> lista = List.of(AlgoritmosExtra.BUSQUEDA_LINEAL, AlgoritmosExtra.BUSQUEDA_BINARIA,
                AlgoritmosExtra.BFS, AlgoritmosExtra.FIBONACCI, AlgoritmosExtra.HANOI);
        AlgoritmosExtra a = lista.get(azar.nextInt(lista.size()));
        return conOpciones(azar, "¿Cuál es la complejidad de " + a.etiqueta() + "?", a.complejidad(),
                java.util.Arrays.stream(AlgoritmosExtra.values()).map(AlgoritmosExtra::complejidad).toList(),
                a.descripcion());
    }

    private static Pregunta estructuraDeRecorrido(Random azar) {
        boolean anchura = azar.nextBoolean();
        return conOpciones(azar, "¿Qué estructura de datos usa el recorrido " + (anchura ? "en anchura (BFS)" : "en profundidad (DFS)") + "?",
                anchura ? "Una cola" : "Una pila", List.of("Una cola", "Una pila", "Un montículo", "Un árbol binario"),
                anchura ? "BFS atiende los nodos en el orden en que se descubren: primero en entrar, primero en salir."
                        : "DFS avanza lo más posible y retrocede al último nodo pendiente: último en entrar, primero en salir.");
    }

    private static Pregunta movimientosDeHanoi(Random azar) {
        int n = 3 + azar.nextInt(3);
        int correcta = (1 << n) - 1;
        List<String> conjunto = List.of(Integer.toString(correcta), Integer.toString(1 << n),
                Integer.toString(2 * n), Integer.toString((1 << (n + 1)) - 1), Integer.toString(n * n));
        return conOpciones(azar, "¿Cuántos movimientos como mínimo necesitan las Torres de Hanoi con " + n + " discos?",
                Integer.toString(correcta), conjunto, "Se necesitan 2^n − 1 movimientos: 2^" + n + " − 1 = " + correcta + ".");
    }
}
