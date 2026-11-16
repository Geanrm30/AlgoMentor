package ni.edu.uam.algomentor.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class MovimientoCeldasTest {

    @Test
    void sinCambiosCadaCeldaConservaSuPosicion() {
        String[] valores = {"5", "3", "1"};

        assertArrayEquals(new int[]{0, 1, 2}, MovimientoCeldas.origenes(valores, valores.clone()));
    }

    @Test
    void intercambioAdyacenteCruzaLasDosCeldas() {
        int[] origen = MovimientoCeldas.origenes(new String[]{"5", "3", "1"}, new String[]{"3", "5", "1"});

        assertArrayEquals(new int[]{1, 0, 2}, origen);
    }

    @Test
    void desplazamientoDeInsercionMueveElClaveYCorreLosDemas() {
        int[] origen = MovimientoCeldas.origenes(new String[]{"2", "4", "6", "3"}, new String[]{"2", "3", "4", "6"});

        assertArrayEquals(new int[]{0, 3, 1, 2}, origen);
    }

    @Test
    void valoresRepetidosNoReutilizanLaMismaCelda() {
        int[] origen = MovimientoCeldas.origenes(new String[]{"4", "4", "1"}, new String[]{"1", "4", "4"});

        assertArrayEquals(new int[]{2, 1, 0}, origen);
    }

    @Test
    void valorQueNoExistiaAntesEsNuevo() {
        int[] origen = MovimientoCeldas.origenes(new String[]{"1", "2"}, new String[]{"1", "9"});

        assertArrayEquals(new int[]{0, -1}, origen);
    }

    @Test
    void entradaDeOtroTamanoMarcaTodasLasCeldasComoNuevas() {
        int[] origen = MovimientoCeldas.origenes(new String[]{"1", "2"}, new String[]{"1", "2", "3"});

        assertArrayEquals(new int[]{-1, -1, -1}, origen);
    }

    @Test
    void sinEstadoPrevioTodasLasCeldasSonNuevas() {
        int[] origen = MovimientoCeldas.origenes(null, new String[]{"7", "8"});

        assertArrayEquals(new int[]{-1, -1}, origen);
    }
}
