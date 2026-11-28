package ni.edu.uam.algomentor.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FondoVidrioTest {

    private static int[] relleno(int argb, int cantidad) {
        int[] datos = new int[cantidad];
        java.util.Arrays.fill(datos, argb);
        return datos;
    }

    @Test void unaCapturaNegraOUniformeSeConsideraVacia() {
        assertTrue(FondoVidrio.pareceVacia(null));
        assertTrue(FondoVidrio.pareceVacia(new int[0]));
        assertTrue(FondoVidrio.pareceVacia(relleno(0xFF000000, 100)));
        assertTrue(FondoVidrio.pareceVacia(relleno(0xFF808080, 100)), "Sin variación no hay nada que mostrar");
        int[] conContraste = {0xFF101010, 0xFFE0E0E0, 0xFF3060A0, 0xFFA05030};
        assertFalse(FondoVidrio.pareceVacia(conContraste));
    }

    @Test void laLuminanciaVaDeNegroABlanco() {
        assertEquals(0, FondoVidrio.luminanciaMedia(relleno(0xFF000000, 10)), 0.001);
        assertEquals(1, FondoVidrio.luminanciaMedia(relleno(0xFFFFFFFF, 10)), 0.001);
        assertEquals(0.5, FondoVidrio.luminanciaMedia(new int[]{0xFF000000, 0xFFFFFFFF}), 0.001);
        assertTrue(FondoVidrio.luminanciaMedia(relleno(0xFF00FF00, 10)) > FondoVidrio.luminanciaMedia(relleno(0xFF0000FF, 10)),
                "El verde pesa más que el azul en la luminosidad percibida");
        assertEquals(0, FondoVidrio.luminanciaMedia(null), 0.001);
    }

    @Test void elTinteCreceConLaLuminosidadEnTemaOscuroYAcotado() {
        double oscuro = FondoVidrio.opacidadTinte(0.05, false, 0.52);
        double medio = FondoVidrio.opacidadTinte(0.5, false, 0.52);
        double claro = FondoVidrio.opacidadTinte(0.95, false, 0.52);
        assertTrue(oscuro < medio && medio < claro, oscuro + " < " + medio + " < " + claro);
        assertTrue(oscuro >= 0.10 && claro <= 0.62);
        assertTrue(oscuro < 0.25, "Con un IDE oscuro detrás no debe quedar negro: " + oscuro);
    }

    @Test void enTemaClaroElTinteSigueSiendoSuficienteParaLeer() {
        for (double l = 0; l <= 1.0001; l += 0.1) {
            double o = FondoVidrio.opacidadTinte(l, true, 0.50);
            assertTrue(o >= 0.32 && o <= 0.70, "l=" + l + " -> " + o);
        }
        assertTrue(FondoVidrio.opacidadTinte(0.1, true, 0.5) > FondoVidrio.opacidadTinte(0.9, true, 0.5));
    }
}
