package ni.edu.uam.algomentor.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormularioLiquidoTest {

    @Test void lasFilasSeApilanConEspacioYLaPastillaVaAlFinal() {
        var d = FormularioLiquido.disponer(new double[]{54, 54, 62});
        assertArrayEquals(new double[]{0, 68, 136}, d.y(), 0.001);
        assertEquals(136 + 62 + FormularioLiquido.ESPACIO, d.yPastilla(), 0.001);
        assertEquals(d.yPastilla() + FormularioLiquido.ALTO_PASTILLA, d.total(), 0.001);
    }

    @Test void sinFilasSoloQuedaLaPastilla() {
        var d = FormularioLiquido.disponer(new double[0]);
        assertEquals(0, d.y().length);
        assertEquals(0, d.yPastilla(), 0.001);
        assertEquals(FormularioLiquido.ALTO_PASTILLA, d.total(), 0.001);
    }

    @Test void elResorteEmpiezaEnCeroTerminaEnUnoYSePasaUnPoco() {
        assertEquals(0, FormularioLiquido.resorte(0), 0.0001);
        assertEquals(0, FormularioLiquido.resorte(-1), 0.0001);
        assertEquals(1, FormularioLiquido.resorte(1), 0.0001);
        assertEquals(1, FormularioLiquido.resorte(2), 0.0001);
        double maximo = 0;
        for (double t = 0; t <= 1; t += 0.01) {
            maximo = Math.max(maximo, FormularioLiquido.resorte(t));
        }
        assertTrue(maximo > 1.0 && maximo < 1.2, "El rebote debe pasarse algo, pero no demasiado: " + maximo);
        assertEquals(1, FormularioLiquido.resorte(0.99), 0.05);
    }

    @Test void laTarjetaDeCristalCortaLasEsquinasOpuestas() {
        List<double[]> p = TarjetaCristal.puntos(400, 300, 34);
        assertEquals(8, p.size());
        assertArrayEquals(new double[]{34, 0}, p.get(0));           // esquina superior izquierda cortada
        assertArrayEquals(new double[]{0, 34}, p.get(p.size() - 1));
        assertArrayEquals(new double[]{400, 266}, p.get(3));        // esquina inferior derecha cortada
        assertArrayEquals(new double[]{366, 300}, p.get(4));
        for (double[] punto : p) {
            assertTrue(punto[0] >= 0 && punto[0] <= 400 && punto[1] >= 0 && punto[1] <= 300);
        }
    }
}
