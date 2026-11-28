package ni.edu.uam.algomentor.util;

import org.junit.jupiter.api.Test;

import static ni.edu.uam.algomentor.util.EfectosBoton.Tipo.*;
import static org.junit.jupiter.api.Assertions.*;

class EfectosBotonTest {

    @Test void cadaTextoDeLaAplicacionTieneElEfectoEsperado() {
        assertEquals(ELIMINAR, EfectosBoton.tipoPara("Eliminar"));
        assertEquals(ACTUALIZAR, EfectosBoton.tipoPara("Actualizar"));
        assertEquals(GUARDAR, EfectosBoton.tipoPara("Guardar"));
        assertEquals(EXPORTAR, EfectosBoton.tipoPara("Exportar PDF"));
        assertEquals(EXPORTAR, EfectosBoton.tipoPara("Exportar a archivo"));
        assertEquals(IMPORTAR, EfectosBoton.tipoPara("Importar desde archivo"));
        assertEquals(GENERAR, EfectosBoton.tipoPara("Generar entrada"));
        assertEquals(GENERAR, EfectosBoton.tipoPara("Generar PDF"));
        assertEquals(LIMPIAR, EfectosBoton.tipoPara("Limpiar"));
        assertEquals(LIMPIAR, EfectosBoton.tipoPara("Restablecer matriz inicial"));
        assertEquals(VER, EfectosBoton.tipoPara("Vista previa"));
        assertEquals(VER, EfectosBoton.tipoPara("Ver historial"));
        assertEquals(VER, EfectosBoton.tipoPara("Ver progreso"));
        assertEquals(BUSCAR, EfectosBoton.tipoPara("Buscar ejercicios"));
        assertEquals(VOLVER, EfectosBoton.tipoPara("Volver al resumen"));
        assertEquals(COMPARAR, EfectosBoton.tipoPara("Comparar"));
        assertEquals(SIMULAR, EfectosBoton.tipoPara("Simular"));
        assertEquals(SIMULAR, EfectosBoton.tipoPara("Resolver y comparar"));
        assertEquals(VERIFICAR, EfectosBoton.tipoPara("Verificar"));
        assertEquals(VERIFICAR, EfectosBoton.tipoPara("Responder"));
    }

    @Test void losBotonesDeNavegacionYLosDinamicosNoRecibenEfecto() {
        for (String texto : new String[]{"Siguiente", "Anterior", "Reproducir", "Pausar", "Comenzar", "Reiniciar",
                "Entrar", "Crear cuenta", "Usar ejemplo", "Pedir una pista", "Siguiente pregunta", "", "   "}) {
            assertNull(EfectosBoton.tipoPara(texto), texto);
        }
        assertNull(EfectosBoton.tipoPara(null));
    }

    @Test void soloLasAccionesSegurasSeAnimanAlHacerClic() {
        // Eliminar, guardar y verificar pueden cancelarse o fallar: las dispara el controlador al terminar bien.
        assertFalse(ELIMINAR.alHacerClic());
        assertFalse(GUARDAR.alHacerClic());
        assertFalse(VERIFICAR.alHacerClic());
        assertTrue(ACTUALIZAR.alHacerClic());
        assertTrue(EXPORTAR.alHacerClic());
        assertTrue(SIMULAR.alHacerClic());
    }

    @Test void todosLosTiposTienenIconoDefinidoEnElCss() throws Exception {
        String css;
        try (var recurso = getClass().getResourceAsStream("/ni/edu/uam/algomentor/css/algomentor.css")) {
            css = new String(recurso.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        for (var tipo : values()) {
            String nombre = tipo == ELIMINAR ? "papelera-cuerpo" : tipo.icono();
            assertTrue(css.contains(".ico-" + nombre + " {"), "Falta .ico-" + nombre);
        }
        for (String extra : new String[]{"check", "cerrar", "papelera-tapa", "menu", "play", "lapiz", "barras", "comparar",
                "libro", "grafo", "estrella", "lista", "tendencia", "informe", "rayo", "reloj", "usuario"}) {
            assertTrue(css.contains(".ico-" + extra + " {"), "Falta .ico-" + extra);
        }
    }
}
