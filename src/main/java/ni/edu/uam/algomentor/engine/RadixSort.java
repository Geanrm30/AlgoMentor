package ni.edu.uam.algomentor.engine;

import java.util.Arrays;
import java.util.List;

/** LSD decimal estable sobre claves long desplazadas: conserva el orden firmado sin negar MIN_VALUE. */
public class RadixSort implements AlgoritmoOrdenamiento {
    @Override public String nombre() { return "Radix Sort"; }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        TrazaOrdenamiento traza = new TrazaOrdenamiento(datos);
        if (datos.length == 1) return traza.terminar();
        int[] auxiliar = new int[datos.length];
        long divisor = 1;
        for (int posicion = 1; posicion <= 10; posicion++) {
            traza.anotar(TipoOperacion.PASADA, List.of(),
                    "Pasada " + posicion + ": dígito con divisor " + divisor
                            + " de la clave (long) valor - Integer.MIN_VALUE. Así se ordenan también los negativos.");
            distribuir(traza, auxiliar, divisor);
            for (int i = 0; i < auxiliar.length; i++) {
                traza.escribir(i, auxiliar[i],
                        "Copiar " + auxiliar[i] + " desde la salida estable a la posición " + (i + 1) + ".");
            }
            divisor *= 10;
        }
        return traza.terminar();
    }

    private void distribuir(TrazaOrdenamiento traza, int[] auxiliar, long divisor) {
        int[] a = traza.datos();
        int[] cuentas = new int[10];
        for (int i = 0; i < a.length; i++) {
            int digito = digito(a[i], divisor);
            cuentas[digito]++;
            traza.anotar(TipoOperacion.CONTAR, List.of(i),
                    "El valor " + a[i] + " tiene dígito " + digito
                            + "; frecuencia = " + cuentas[digito] + ".");
        }
        for (int i = 1; i < cuentas.length; i++) cuentas[i] += cuentas[i - 1];
        traza.anotar(TipoOperacion.PASADA, List.of(),
                "Límites acumulados de los dígitos: " + Arrays.toString(cuentas)
                        + ". Recorrer de derecha a izquierda preserva el orden de los dígitos iguales.");
        for (int i = a.length - 1; i >= 0; i--) {
            int digito = digito(a[i], divisor);
            int destino = --cuentas[digito];
            auxiliar[destino] = a[i];
            traza.anotar(TipoOperacion.DISTRIBUIR, List.of(i),
                    "Distribuir " + a[i] + " (dígito " + digito + ") en salida auxiliar["
                            + (destino + 1) + "]; se mantiene estable el orden previo.");
        }
    }

    private int digito(int valor, long divisor) {
        return (int) ((((long) valor - Integer.MIN_VALUE) / divisor) % 10);
    }
}
