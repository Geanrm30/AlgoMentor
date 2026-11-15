package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GaussTest {

    private final Gauss gauss = new Gauss();

    private static Fraccion[][] m(String estado) {
        return FormatoDatos.leerEstadoMatriz(estado);
    }

    private static boolean hayPaso(ResultadoMatriz r, TipoOperacion tipo) {
        return r.ejecucion().pasos().stream().anyMatch(p -> p.tipo() == tipo);
    }

    @Test
    void resuelveUnSistemaDe2x2() {
        ResultadoMatriz r = gauss.ejecutar(m("2,1,5|1,-1,1"));
        assertEquals(ClasificacionSistema.UNICA, r.clasificacion());
        assertEquals(List.of(Fraccion.de(2), Fraccion.de(1)), r.solucion());
        assertEquals("2,1,5|0,-3/2,-3/2", r.ejecucion().estadoFinal());
        assertTrue(hayPaso(r, TipoOperacion.ELIMINAR_FILA));
        assertTrue(hayPaso(r, TipoOperacion.SUSTITUIR_ATRAS));
    }

    @Test
    void resuelveUnSistemaDe3x3() {
        ResultadoMatriz r = gauss.ejecutar(m("1,1,1,6|0,2,5,-4|2,5,-1,27"));
        assertEquals(ClasificacionSistema.UNICA, r.clasificacion());
        assertEquals(List.of(Fraccion.de(5), Fraccion.de(3), Fraccion.de(-2)), r.solucion());
    }

    @Test
    void intercambiaFilasCuandoElPivoteEsCero() {
        ResultadoMatriz r = gauss.ejecutar(m("0,1,2|1,1,3"));
        assertEquals(ClasificacionSistema.UNICA, r.clasificacion());
        assertEquals(List.of(Fraccion.de(1), Fraccion.de(2)), r.solucion());
        assertEquals(TipoOperacion.INTERCAMBIAR_FILAS, r.ejecucion().pasos().get(0).tipo());
    }

    @Test
    void usaFraccionesExactas() {
        ResultadoMatriz r = gauss.ejecutar(m("3,1,1|1,2,1"));
        assertEquals(List.of(Fraccion.de(1, 5), Fraccion.de(2, 5)), r.solucion());
    }

    @Test
    void detectaSistemaSinSolucion() {
        ResultadoMatriz r = gauss.ejecutar(m("1,1,2|1,1,3"));
        assertEquals(ClasificacionSistema.SIN_SOLUCION, r.clasificacion());
        assertTrue(r.solucion().isEmpty());
    }

    @Test
    void detectaSistemaConInfinitasSoluciones() {
        ResultadoMatriz r = gauss.ejecutar(m("1,1,2|2,2,4"));
        assertEquals(ClasificacionSistema.INFINITAS, r.clasificacion());
        assertTrue(r.solucion().isEmpty());
    }

    @Test
    void detectaMenosEcuacionesQueIncognitas() {
        ResultadoMatriz r = gauss.ejecutar(m("1,1,1,3"));
        assertEquals(ClasificacionSistema.INFINITAS, r.clasificacion());
    }

    @Test
    void noModificaLaMatrizOriginal() {
        Fraccion[][] original = m("2,1,5|1,-1,1");
        gauss.ejecutar(original);
        assertEquals("2,1,5|1,-1,1", FormatoDatos.matrizAEstado(original));
    }

    @Test
    void cadaPasoEncadenaConElSiguiente() {
        ResultadoMatriz r = gauss.ejecutar(m("1,1,1,6|0,2,5,-4|2,5,-1,27"));
        List<Paso> pasos = r.ejecucion().pasos();
        assertEquals(r.ejecucion().estadoInicial(), pasos.get(0).estadoAnterior());
        for (int i = 1; i < pasos.size(); i++) {
            assertEquals(pasos.get(i - 1).estadoPosterior(), pasos.get(i).estadoAnterior());
            assertEquals(i + 1, pasos.get(i).numero());
        }
        assertEquals(TipoOperacion.FINALIZAR, pasos.get(pasos.size() - 1).tipo());
    }

    @Test
    void rechazaMatricesInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> gauss.ejecutar(null));
        assertThrows(IllegalArgumentException.class,
                () -> gauss.ejecutar(new Fraccion[][]{{Fraccion.UNO, Fraccion.UNO}, {Fraccion.UNO}}));
    }
}
