package ni.edu.uam.algomentor.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.model.Algoritmo;
import ni.edu.uam.algomentor.util.ValidacionesFormulario;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaqueteNuevosAlgoritmosTest {
    @Test void intercambiaNombresYDatosSinDependerDeIdentificadoresLocales() throws Exception {
        var paquete = new PaqueteEjercicios(1, CatalogoAlgoritmos.ordenamientos().stream()
                .map(a -> new EjercicioIntercambio(a.nombre(), "v1;ARREGLO;-2147483648,0,2147483647", 2, "MANUAL"))
                .toList());
        ObjectMapper json = new ObjectMapper();
        String contenido = json.writeValueAsString(paquete);
        assertFalse(contenido.contains("_id"));
        PaqueteEjercicios recuperado = json.readValue(contenido, PaqueteEjercicios.class);
        assertEquals(paquete, recuperado);
        for (var ejercicio : recuperado.ejercicios()) {
            Algoritmo algoritmo = new Algoritmo();
            algoritmo.setNombre(ejercicio.algoritmo());
            assertDoesNotThrow(() -> ValidacionesFormulario.validarEjercicio(algoritmo,
                    ejercicio.origen(), ejercicio.dificultad(), ejercicio.datos()));
        }
    }
}
