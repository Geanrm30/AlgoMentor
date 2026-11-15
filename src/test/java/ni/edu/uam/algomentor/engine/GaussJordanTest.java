package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GaussJordanTest {

    private final GaussJordan gaussJordan = new GaussJordan();

    private static Fraccion[][] m(String estado) {
        return FormatoDatos.leerEstadoMatriz(estado);
    }

    private static boolean hayPaso(ResultadoMatriz r, TipoOperacion tipo) {
        return r.ejecucion().pasos().stream().anyMatch(p -> p.tipo() == tipo);
    }

    @Test
    void reduceUnSistemaDe2x2ALaIdentidad() {
        ResultadoMatriz r = gaussJordan.ejecutar(m("2,1,5|1,-1,1"));
        assertEquals(ClasificacionSistema.UNICA, r.clasificacion());
        assertEquals("1,0,2|0,1,1", r.ejecucion().estadoFinal());
        assertEquals(List.of(Fraccion.de(2), Fraccion.de(1)), r.solucion());
        assertTrue(hayPaso(r, TipoOperacion.ESCALAR_FILA));
    }

    @Test
    void resuelveUnSistemaDe3x3() {
        ResultadoMatriz r = gaussJordan.ejecutar(m("1,1,1,6|0,2,5,-4|2,5,-1,27"));
        assertEquals(List.of(Fraccion.de(5), Fraccion.de(3), Fraccion.de(-2)), r.solucion());
        assertEquals("1,0,0,5|0,1,0,3|0,0,1,-2", r.ejecucion().estadoFinal());
    }

    @Test
    void coincideConGaussEnLaSolucion() {
        Fraccion[][] sistema = m("3,1,1|1,2,1");
        assertEquals(new Gauss().ejecutar(sistema).solucion(), gaussJordan.ejecutar(sistema).solucion());
        assertEquals(List.of(Fraccion.de(1, 5), Fraccion.de(2, 5)), gaussJordan.ejecutar(sistema).solucion());
    }

    @Test
    void noEscalaSiElPivoteYaEsUno() {
        ResultadoMatriz r = gaussJordan.ejecutar(m("1,0,3|0,1,4"));
        assertFalse(hayPaso(r, TipoOperacion.ESCALAR_FILA));
        assertFalse(hayPaso(r, TipoOperacion.ELIMINAR_FILA));
        assertEquals(List.of(Fraccion.de(3), Fraccion.de(4)), r.solucion());
    }

    @Test
    void intercambiaFilasCuandoElPivoteEsCero() {
        ResultadoMatriz r = gaussJordan.ejecutar(m("0,1,2|1,1,3"));
        assertEquals(List.of(Fraccion.de(1), Fraccion.de(2)), r.solucion());
        assertEquals(TipoOperacion.INTERCAMBIAR_FILAS, r.ejecucion().pasos().get(0).tipo());
    }

    @Test
    void detectaSistemaSinSolucion() {
        ResultadoMatriz r = gaussJordan.ejecutar(m("1,1,2|1,1,3"));
        assertEquals(ClasificacionSistema.SIN_SOLUCION, r.clasificacion());
        assertTrue(r.solucion().isEmpty());
    }

    @Test
    void detectaSistemaConInfinitasSoluciones() {
        ResultadoMatriz r = gaussJordan.ejecutar(m("1,1,2|2,2,4"));
        assertEquals(ClasificacionSistema.INFINITAS, r.clasificacion());
        assertEquals("1,1,2|0,0,0", r.ejecucion().estadoFinal());
    }

    @Test
    void noModificaLaMatrizOriginal() {
        Fraccion[][] original = m("2,1,5|1,-1,1");
        gaussJordan.ejecutar(original);
        assertEquals("2,1,5|1,-1,1", FormatoDatos.matrizAEstado(original));
    }

    @Test
    void cadaPasoEncadenaConElSiguiente() {
        ResultadoMatriz r = gaussJordan.ejecutar(m("2,1,-1,8|-3,-1,2,-11|-2,1,2,-3"));
        List<Paso> pasos = r.ejecucion().pasos();
        assertEquals(r.ejecucion().estadoInicial(), pasos.get(0).estadoAnterior());
        for (int i = 1; i < pasos.size(); i++) {
            assertEquals(pasos.get(i - 1).estadoPosterior(), pasos.get(i).estadoAnterior());
        }
        assertEquals(List.of(Fraccion.de(2), Fraccion.de(3), Fraccion.de(-1)), r.solucion());
    }
}
