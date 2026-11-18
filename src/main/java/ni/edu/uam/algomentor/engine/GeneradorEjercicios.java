package ni.edu.uam.algomentor.engine;

import java.util.Random;

/**
 * Genera datos de ejercicios de forma controlada: con una semilla el resultado se puede repetir,
 * y los limites evitan entradas que la interfaz no pueda mostrar.
 */
public class GeneradorEjercicios {

    private static final int LIMITE_VALOR = 1000;
    private static final int MAX_COEFICIENTE = 20;
    private static final int INTENTOS = 200;

    private final Random azar;

    public GeneradorEjercicios(long semilla) {
        this.azar = new Random(semilla);
    }

    public GeneradorEjercicios() {
        this(System.nanoTime());
    }

    /** Datos versionados, listos para guardar y usar en práctica o simulación. */
    public String datosPara(String nombreAlgoritmo, int dificultad) {
        if (dificultad < 1 || dificultad > 5) {
            throw new IllegalArgumentException("La dificultad debe estar entre 1 y 5.");
        }
        CatalogoAlgoritmos algoritmo = CatalogoAlgoritmos.porNombre(nombreAlgoritmo);
        if (algoritmo.esOrdenamiento()) {
            return FormatoDatos.serializarArreglo(arregloAleatorio(
                    Math.min(12, 4 + dificultad * 2), -20 * dificultad, 20 * dificultad));
        }
        return FormatoDatos.serializarMatriz(sistemaAleatorio(Math.min(4, dificultad + 1), dificultad * 3));
    }

    public int[] arregloAleatorio(int tamano, int minimo, int maximo) {
        if (tamano < 1 || tamano > FormatoDatos.MAX_ELEMENTOS) {
            throw new IllegalArgumentException("El tamano debe estar entre 1 y " + FormatoDatos.MAX_ELEMENTOS + ".");
        }
        if (minimo > maximo || minimo < -LIMITE_VALOR || maximo > LIMITE_VALOR) {
            throw new IllegalArgumentException("El rango debe estar entre " + -LIMITE_VALOR + " y " + LIMITE_VALOR
                    + ", con minimo menor o igual al maximo.");
        }
        int[] arreglo = new int[tamano];
        for (int i = 0; i < tamano; i++) {
            arreglo[i] = minimo + azar.nextInt(maximo - minimo + 1);
        }
        return arreglo;
    }

    /** Matriz ampliada n x (n+1) de un sistema con solucion unica y coeficientes enteros. */
    public Fraccion[][] sistemaAleatorio(int incognitas, int maxCoeficiente) {
        if (incognitas < 2 || incognitas > FormatoDatos.MAX_FILAS || incognitas + 1 > FormatoDatos.MAX_COLUMNAS) {
            throw new IllegalArgumentException("Las incognitas deben estar entre 2 y "
                    + Math.min(FormatoDatos.MAX_FILAS, FormatoDatos.MAX_COLUMNAS - 1) + ".");
        }
        if (maxCoeficiente < 1 || maxCoeficiente > MAX_COEFICIENTE) {
            throw new IllegalArgumentException("El coeficiente maximo debe estar entre 1 y " + MAX_COEFICIENTE + ".");
        }
        Gauss verificador = new Gauss();
        for (int intento = 0; intento < INTENTOS; intento++) {
            Fraccion[][] matriz = new Fraccion[incognitas][incognitas + 1];
            int[] solucion = new int[incognitas];
            for (int j = 0; j < incognitas; j++) {
                solucion[j] = entero(maxCoeficiente);
            }
            for (int i = 0; i < incognitas; i++) {
                long termino = 0;
                for (int j = 0; j < incognitas; j++) {
                    int coeficiente = entero(maxCoeficiente);
                    matriz[i][j] = Fraccion.de(coeficiente);
                    termino += (long) coeficiente * solucion[j];
                }
                matriz[i][incognitas] = Fraccion.de(termino);
            }
            if (verificador.ejecutar(matriz).clasificacion() == ClasificacionSistema.UNICA) {
                return matriz;
            }
        }
        throw new IllegalStateException("No se pudo generar un sistema con solucion unica.");
    }

    private int entero(int maximo) {
        return azar.nextInt(2 * maximo + 1) - maximo;
    }
}
