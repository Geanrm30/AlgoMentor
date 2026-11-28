package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneradorEjerciciosTest {

    @Test void generaEjerciciosUtilizablesParaTodosLosAlgoritmos() {
        for (var algoritmo : CatalogoAlgoritmos.values()) {
            for (int dificultad = 1; dificultad <= 5; dificultad++) {
                String datos = new GeneradorEjercicios(42).datosPara(algoritmo.nombre(), dificultad);
                if (algoritmo.esOrdenamiento()) {
                    int[] entrada = FormatoDatos.leerArreglo(datos);
                    assertTrue(entrada.length <= FormatoDatos.MAX_ELEMENTOS_VISUALES);
                    if (dificultad == 5) assertEquals(FormatoDatos.MAX_ELEMENTOS_VISUALES, entrada.length);
                    String respuesta = algoritmo.crearOrdenamiento().ejecutar(entrada).estadoFinal();
                    assertTrue(ni.edu.uam.algomentor.service.EvaluadorPractica.evaluar(algoritmo.nombre(), datos,
                            respuesta.substring(1, respuesta.length() - 1)).correcto());
                } else {
                    assertEquals(ClasificacionSistema.UNICA,
                            algoritmo.crearMatricial().ejecutar(FormatoDatos.leerMatriz(datos)).clasificacion());
                }
            }
        }
        assertThrows(IllegalArgumentException.class, () -> new GeneradorEjercicios().datosPara("Quick Sort", 0));
        assertThrows(IllegalArgumentException.class, () -> new GeneradorEjercicios().datosPara("Quick Sort", 6));
    }

    @Test
    void conLaMismaSemillaGeneraLoMismo() {
        int[] a = new GeneradorEjercicios(42).arregloAleatorio(10, -50, 50);
        int[] b = new GeneradorEjercicios(42).arregloAleatorio(10, -50, 50);
        assertArrayEquals(a, b);
    }

    @Test
    void elArregloRespetaTamanoYRango() {
        int[] arreglo = new GeneradorEjercicios(7).arregloAleatorio(FormatoDatos.MAX_ELEMENTOS, -5, 5);
        assertEquals(FormatoDatos.MAX_ELEMENTOS, arreglo.length);
        for (int valor : arreglo) {
            assertTrue(valor >= -5 && valor <= 5);
        }
    }

    @Test
    void elArregloGeneradoSePuedeGuardarYLeer() {
        int[] arreglo = new GeneradorEjercicios(3).arregloAleatorio(8, 0, 99);
        assertArrayEquals(arreglo, FormatoDatos.leerArreglo(FormatoDatos.serializarArreglo(arreglo)));
    }

    @Test
    void rechazaParametrosInvalidosDelArreglo() {
        GeneradorEjercicios generador = new GeneradorEjercicios(1);
        assertThrows(IllegalArgumentException.class, () -> generador.arregloAleatorio(0, 0, 9));
        assertThrows(IllegalArgumentException.class,
                () -> generador.arregloAleatorio(FormatoDatos.MAX_ELEMENTOS + 1, 0, 9));
        assertThrows(IllegalArgumentException.class, () -> generador.arregloAleatorio(5, 9, 0));
        assertThrows(IllegalArgumentException.class, () -> generador.arregloAleatorio(5, 0, 5000));
    }

    @Test
    void elSistemaGeneradoSiempreTieneSolucionUnica() {
        for (long semilla = 1; semilla <= 25; semilla++) {
            GeneradorEjercicios generador = new GeneradorEjercicios(semilla);
            for (int incognitas = 2; incognitas <= 4; incognitas++) {
                Fraccion[][] sistema = generador.sistemaAleatorio(incognitas, 5);
                assertEquals(incognitas, sistema.length);
                assertEquals(incognitas + 1, sistema[0].length);
                assertEquals(ClasificacionSistema.UNICA, new Gauss().ejecutar(sistema).clasificacion());
                assertEquals(ClasificacionSistema.UNICA, new GaussJordan().ejecutar(sistema).clasificacion());
            }
        }
    }

    @Test
    void elSistemaGeneradoSePuedeGuardarYLeer() {
        Fraccion[][] sistema = new GeneradorEjercicios(11).sistemaAleatorio(3, 9);
        Fraccion[][] leido = FormatoDatos.leerMatriz(FormatoDatos.serializarMatriz(sistema));
        assertEquals(FormatoDatos.matrizAEstado(sistema), FormatoDatos.matrizAEstado(leido));
    }

    @Test
    void conLaMismaSemillaElSistemaEsIgual() {
        Fraccion[][] a = new GeneradorEjercicios(5).sistemaAleatorio(3, 5);
        Fraccion[][] b = new GeneradorEjercicios(5).sistemaAleatorio(3, 5);
        assertEquals(FormatoDatos.matrizAEstado(a), FormatoDatos.matrizAEstado(b));
    }

    @Test
    void rechazaParametrosInvalidosDelSistema() {
        GeneradorEjercicios generador = new GeneradorEjercicios(1);
        assertThrows(IllegalArgumentException.class, () -> generador.sistemaAleatorio(1, 5));
        assertThrows(IllegalArgumentException.class, () -> generador.sistemaAleatorio(7, 5));
        assertThrows(IllegalArgumentException.class, () -> generador.sistemaAleatorio(3, 0));
        assertThrows(IllegalArgumentException.class, () -> generador.sistemaAleatorio(3, 21));
    }
}
