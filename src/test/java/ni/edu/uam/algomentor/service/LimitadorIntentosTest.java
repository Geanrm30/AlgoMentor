package ni.edu.uam.algomentor.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LimitadorIntentosTest {

    private final AtomicLong reloj = new AtomicLong(1_000);
    private final LimitadorIntentos limitador =
            new LimitadorIntentos(5, Duration.ofSeconds(30), reloj::get);

    @Test
    void noBloqueaAntesDelQuintoFallo() {
        for (int i = 0; i < 4; i++) {
            limitador.registrarFallo("ana");
        }
        assertEquals(0, limitador.segundosRestantes("ana"));
    }

    @Test
    void bloqueaTrasElQuintoFalloYSeLiberaCuandoPasaElTiempo() {
        for (int i = 0; i < 5; i++) {
            limitador.registrarFallo("ana");
        }
        assertEquals(30, limitador.segundosRestantes("ana"));

        reloj.addAndGet(10_000);
        assertEquals(20, limitador.segundosRestantes("ana"));

        reloj.addAndGet(20_000);
        assertEquals(0, limitador.segundosRestantes("ana"));
    }

    @Test
    void despuesDelBloqueoElContadorEmpiezaDeNuevo() {
        for (int i = 0; i < 5; i++) {
            limitador.registrarFallo("ana");
        }
        reloj.addAndGet(31_000);
        assertEquals(0, limitador.segundosRestantes("ana"));

        for (int i = 0; i < 4; i++) {
            limitador.registrarFallo("ana");
        }
        assertEquals(0, limitador.segundosRestantes("ana"));
    }

    @Test
    void unInicioDeSesionCorrectoReiniciaLosFallos() {
        for (int i = 0; i < 4; i++) {
            limitador.registrarFallo("ana");
        }
        limitador.registrarExito("ana");
        limitador.registrarFallo("ana");
        assertEquals(0, limitador.segundosRestantes("ana"));
    }

    @Test
    void cadaUsuarioTieneSuPropioContador() {
        for (int i = 0; i < 5; i++) {
            limitador.registrarFallo("ana");
        }
        assertEquals(0, limitador.segundosRestantes("luis"));
    }

    @Test
    void rechazaUnMaximoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> new LimitadorIntentos(0, Duration.ofSeconds(30)));
    }
}
