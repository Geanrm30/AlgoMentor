package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.engine.FormatoDatos;
import ni.edu.uam.algomentor.engine.GeneradorEjercicios;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TutorServiceTest {

    @Test void daTresPistasParaCadaAlgoritmoSinRevelarLaRespuestaFinal() {
        for (CatalogoAlgoritmos algoritmo : CatalogoAlgoritmos.values()) {
            for (int dificultad = 1; dificultad <= 5; dificultad++) {
                String datos = new GeneradorEjercicios(dificultad).datosPara(algoritmo.nombre(), dificultad);
                var pistas = TutorService.pistas(algoritmo.nombre(), datos);
                assertEquals(TutorService.MAX_PISTAS, pistas.size(), algoritmo.nombre());
                pistas.forEach(p -> assertFalse(p.isBlank()));
                if (algoritmo.esOrdenamiento()) {
                    String final_ = algoritmo.crearOrdenamiento().ejecutar(FormatoDatos.leerArreglo(datos)).estadoFinal();
                    String desordenado = Arrays.toString(FormatoDatos.leerArreglo(datos));
                    if (!desordenado.equals(final_)) {
                        for (String pista : pistas) {
                            assertFalse(pista.contains(final_), algoritmo.nombre() + " revela la respuesta: " + pista);
                        }
                    }
                }
            }
        }
    }

    @Test void arregloYaOrdenadoNoNecesitaCambios() {
        var pistas = TutorService.pistas("Bubble Sort", FormatoDatos.serializarArreglo(new int[]{1, 2, 3}));
        assertTrue(pistas.get(1).contains("ya está ordenado"));
    }

    @Test void consejosPorTipoDeError() {
        assertTrue(TutorService.consejoPorError("SIGNO").contains("signos"));
        assertTrue(TutorService.consejoPorError("FRACCION").contains("fracciones"));
        assertEquals("", TutorService.consejoPorError("OTRO"));
        assertEquals("", TutorService.consejoPorError(null));
    }
}
