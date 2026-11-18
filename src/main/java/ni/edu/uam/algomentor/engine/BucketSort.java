package ni.edu.uam.algomentor.engine;

import java.util.ArrayList;
import java.util.List;

/** Cubetas de intervalos uniformes, ordenadas por inserción estable. La aritmética del rango usa long. */
public class BucketSort implements AlgoritmoOrdenamiento {
    private record Elemento(int valor, int origen) { }

    @Override public String nombre() { return "Bucket Sort"; }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        TrazaOrdenamiento traza = new TrazaOrdenamiento(datos);
        if (datos.length == 1) return traza.terminar();
        int minimo = datos[0];
        int maximo = datos[0];
        for (int valor : datos) {
            minimo = Math.min(minimo, valor);
            maximo = Math.max(maximo, valor);
        }
        int cantidad = Math.max(1, (int) Math.sqrt(datos.length));
        long rango = (long) maximo - minimo + 1;
        List<List<Elemento>> cubetas = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) cubetas.add(new ArrayList<>());
        traza.anotar(TipoOperacion.PASADA, List.of(),
                "Crear " + cantidad + " cubetas para el intervalo [" + minimo + ", " + maximo
                        + "]. Cubeta = (valor - mínimo) * cantidad / rango, calculada con long.");
        for (int i = 0; i < datos.length; i++) {
            int indice = (int) ((((long) datos[i] - minimo) * cantidad) / rango);
            cubetas.get(indice).add(new Elemento(datos[i], i));
            traza.anotar(TipoOperacion.DISTRIBUIR, List.of(i),
                    "Distribuir " + datos[i] + " en la cubeta " + (indice + 1)
                            + ": " + valores(cubetas.get(indice)) + ".");
        }
        int destino = 0;
        for (int i = 0; i < cubetas.size(); i++) {
            List<Elemento> cubeta = cubetas.get(i);
            ordenarCubeta(traza, cubeta, i + 1);
            for (Elemento elemento : cubeta) {
                traza.escribir(destino++, elemento.valor(),
                        "Concatenar la cubeta " + (i + 1) + ": escribir " + elemento.valor() + ".");
            }
        }
        return traza.terminar();
    }

    private void ordenarCubeta(TrazaOrdenamiento traza, List<Elemento> cubeta, int numero) {
        traza.anotar(TipoOperacion.ORDENAR_CUBETA, List.of(),
                "Ordenar por inserción la cubeta " + numero + ": " + valores(cubeta) + ".");
        for (int i = 1; i < cubeta.size(); i++) {
            Elemento clave = cubeta.get(i);
            int j = i - 1;
            while (j >= 0) {
                Elemento anterior = cubeta.get(j);
                traza.anotar(TipoOperacion.COMPARAR, List.of(anterior.origen(), clave.origen()),
                        "Cubeta " + numero + ": comparar " + anterior.valor() + " con " + clave.valor() + ".");
                if (anterior.valor() <= clave.valor()) break;
                cubeta.set(j + 1, anterior);
                traza.anotar(TipoOperacion.ESCRIBIR_AUXILIAR, List.of(anterior.origen()),
                        "Desplazar " + anterior.valor() + " a la posición " + (j + 2)
                                + " dentro de la cubeta " + numero + ": " + valores(cubeta) + ".");
                j--;
            }
            cubeta.set(j + 1, clave);
            traza.anotar(TipoOperacion.ESCRIBIR_AUXILIAR, List.of(clave.origen()),
                    "Insertar " + clave.valor() + " en la cubeta " + numero + ": " + valores(cubeta) + ".");
        }
        traza.anotar(TipoOperacion.ORDENAR_CUBETA, List.of(),
                "Cubeta " + numero + " ordenada: " + valores(cubeta) + ".");
    }

    private List<Integer> valores(List<Elemento> cubeta) {
        return cubeta.stream().map(Elemento::valor).toList();
    }
}
