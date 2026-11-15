package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerificadorOperacionesTest {

    private final VerificadorOperaciones verificador = new VerificadorOperaciones();
    private final Fraccion[][] anterior = m("2,1,5|1,-1,1");

    private static Fraccion[][] m(String estado) {
        return FormatoDatos.leerEstadoMatriz(estado);
    }

    // F2 = F2 - (1/2) * F1  ->  0,-3/2,-3/2
    private final OperacionFila eliminacion = OperacionFila.eliminar(1, 0, Fraccion.de(1, 2));

    @Test
    void aceptaUnaEliminacionCorrecta() {
        ResultadoVerificacion r = verificador.verificar(anterior, eliminacion, m("2,1,5|0,-3/2,-3/2"));
        assertTrue(r.correcto());
        assertTrue(r.errores().isEmpty());
    }

    @Test
    void aceptaUnIntercambioCorrecto() {
        ResultadoVerificacion r = verificador.verificar(anterior, OperacionFila.intercambiar(0, 1), m("1,-1,1|2,1,5"));
        assertTrue(r.correcto());
    }

    @Test
    void aceptaUnEscaladoCorrecto() {
        ResultadoVerificacion r = verificador.verificar(anterior, OperacionFila.escalar(0, Fraccion.de(1, 2)),
                m("1,1/2,5/2|1,-1,1"));
        assertTrue(r.correcto());
    }

    @Test
    void detectaUnErrorDeSigno() {
        ResultadoVerificacion r = verificador.verificar(anterior, eliminacion, m("2,1,5|0,3/2,-3/2"));
        assertFalse(r.correcto());
        assertEquals(1, r.errores().size());
        ErrorOperacion error = r.errores().get(0);
        assertEquals(TipoError.SIGNO, error.tipo());
        assertEquals(1, error.fila());
        assertEquals(1, error.columna());
    }

    @Test
    void detectaUnErrorAritmetico() {
        ResultadoVerificacion r = verificador.verificar(anterior, eliminacion, m("2,1,5|0,-3/2,-1"));
        assertEquals(1, r.errores().size());
        assertEquals(TipoError.ARITMETICO, r.errores().get(0).tipo());
        assertEquals(2, r.errores().get(0).columna());
    }

    @Test
    void detectaUnErrorDeFraccion() {
        ResultadoVerificacion r = verificador.verificar(anterior, eliminacion, m("2,1,5|0,-2/3,-3/2"));
        assertEquals(1, r.errores().size());
        assertEquals(TipoError.FRACCION, r.errores().get(0).tipo());
    }

    @Test
    void detectaCuandoSeModificaUnaFilaQueNoParticipa() {
        ResultadoVerificacion r = verificador.verificar(anterior, eliminacion, m("3,1,5|0,-3/2,-3/2"));
        assertFalse(r.correcto());
        assertEquals(TipoError.OPERACION_INVALIDA, r.errores().get(0).tipo());
        assertEquals(0, r.errores().get(0).fila());
    }

    @Test
    void rechazaOperacionesInvalidas() {
        Fraccion[][] igual = m("2,1,5|1,-1,1");
        assertEquals(TipoError.OPERACION_INVALIDA,
                verificador.verificar(anterior, OperacionFila.eliminar(0, 0, Fraccion.UNO), igual).errores().get(0).tipo());
        assertEquals(TipoError.OPERACION_INVALIDA,
                verificador.verificar(anterior, OperacionFila.escalar(0, Fraccion.CERO), igual).errores().get(0).tipo());
        assertEquals(TipoError.OPERACION_INVALIDA,
                verificador.verificar(anterior, OperacionFila.intercambiar(0, 5), igual).errores().get(0).tipo());
        assertEquals(TipoError.OPERACION_INVALIDA,
                verificador.verificar(anterior, OperacionFila.intercambiar(1, 1), igual).errores().get(0).tipo());
    }

    @Test
    void rechazaMatricesConOtrasDimensiones() {
        assertEquals(TipoError.OPERACION_INVALIDA,
                verificador.verificar(anterior, eliminacion, m("2,1|0,-3/2")).errores().get(0).tipo());
        assertEquals(TipoError.OPERACION_INVALIDA,
                verificador.verificar(anterior, eliminacion, null).errores().get(0).tipo());
    }

    @Test
    void noModificaLaMatrizAnterior() {
        verificador.verificar(anterior, eliminacion, m("2,1,5|0,-3/2,-3/2"));
        assertEquals("2,1,5|1,-1,1", FormatoDatos.matrizAEstado(anterior));
    }

    @Test
    void losTiposDeErrorCoincidenConLaBaseDeDatos() {
        assertEquals("[SIGNO, FRACCION, ARITMETICO, OPERACION_INVALIDA]", java.util.Arrays.toString(TipoError.values()));
    }
}
