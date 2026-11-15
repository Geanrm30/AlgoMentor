package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormatoMatrizTest {

    @Test
    void serializaYLeeUnaMatrizConFracciones() {
        Fraccion[][] matriz = {
                {Fraccion.de(1), Fraccion.de(1, 2)},
                {Fraccion.de(3), Fraccion.de(-4)}
        };
        String texto = FormatoDatos.serializarMatriz(matriz);
        assertEquals("v1;MATRIZ;2x2;1,1/2|3,-4", texto);
        assertEquals(FormatoDatos.matrizAEstado(matriz), FormatoDatos.matrizAEstado(FormatoDatos.leerMatriz(texto)));
    }

    @Test
    void leeUnEstadoDePaso() {
        Fraccion[][] matriz = FormatoDatos.leerEstadoMatriz("2,1,5|1,-1,1");
        assertEquals(2, matriz.length);
        assertEquals(3, matriz[0].length);
        assertEquals(Fraccion.de(-1), matriz[1][1]);
    }

    @Test
    void rechazaFormatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> FormatoDatos.leerMatriz(null));
        assertThrows(IllegalArgumentException.class, () -> FormatoDatos.leerMatriz("v2;MATRIZ;1x2;1,2"));
        assertThrows(IllegalArgumentException.class, () -> FormatoDatos.leerMatriz("v1;ARREGLO;1,2"));
        assertThrows(IllegalArgumentException.class, () -> FormatoDatos.leerMatriz("v1;MATRIZ;2x2;1,2"));
        assertThrows(IllegalArgumentException.class, () -> FormatoDatos.leerMatriz("v1;MATRIZ;2x2;1,2|3"));
        assertThrows(IllegalArgumentException.class, () -> FormatoDatos.leerMatriz("v1;MATRIZ;1x2;1,"));
        assertThrows(IllegalArgumentException.class, () -> FormatoDatos.leerMatriz("v1;MATRIZ;1x1;5"));
        assertThrows(IllegalArgumentException.class, () -> FormatoDatos.leerMatriz("v1;MATRIZ;1x2;1,x"));
    }

    @Test
    void respetaLosLimitesDeTamano() {
        Fraccion[][] demasiadasFilas = new Fraccion[FormatoDatos.MAX_FILAS + 1][2];
        for (Fraccion[] fila : demasiadasFilas) {
            fila[0] = Fraccion.UNO;
            fila[1] = Fraccion.UNO;
        }
        assertThrows(IllegalArgumentException.class, () -> FormatoDatos.validarMatriz(demasiadasFilas));
        assertThrows(IllegalArgumentException.class, () -> FormatoDatos.validarMatriz(null));
        assertThrows(IllegalArgumentException.class,
                () -> FormatoDatos.validarMatriz(new Fraccion[][]{{Fraccion.UNO, Fraccion.UNO}, {Fraccion.UNO}}));
    }
}
