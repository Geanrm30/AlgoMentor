package ni.edu.uam.algomentor.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Limita los intentos de inicio de sesión fallidos por usuario. Tras {@code maximoFallos}
 * fallos seguidos, el usuario queda bloqueado durante {@code duracionBloqueo}. El estado vive en
 * memoria: se reinicia al cerrar la aplicación y no se comparte entre equipos.
 */
public final class LimitadorIntentos {

    private final int maximoFallos;
    private final long duracionBloqueoMs;
    private final LongSupplier relojMs;
    private final Map<String, Registro> registros = new HashMap<>();

    public LimitadorIntentos(int maximoFallos, Duration duracionBloqueo) {
        this(maximoFallos, duracionBloqueo, System::currentTimeMillis);
    }

    LimitadorIntentos(int maximoFallos, Duration duracionBloqueo, LongSupplier relojMs) {
        if (maximoFallos < 1) {
            throw new IllegalArgumentException("El máximo de fallos debe ser positivo.");
        }
        this.maximoFallos = maximoFallos;
        this.duracionBloqueoMs = Objects.requireNonNull(duracionBloqueo).toMillis();
        this.relojMs = Objects.requireNonNull(relojMs);
    }

    /** Segundos que faltan para poder intentar de nuevo; 0 si no hay bloqueo. */
    public synchronized long segundosRestantes(String clave) {
        Registro registro = registros.get(clave);
        if (registro == null || registro.bloqueadoHastaMs == 0) {
            return 0;
        }
        long restanteMs = registro.bloqueadoHastaMs - relojMs.getAsLong();
        if (restanteMs <= 0) {
            registros.remove(clave);
            return 0;
        }
        return (restanteMs + 999) / 1000;
    }

    public synchronized void registrarFallo(String clave) {
        Registro registro = registros.computeIfAbsent(clave, k -> new Registro());
        registro.fallos++;
        if (registro.fallos >= maximoFallos) {
            registro.fallos = 0;
            registro.bloqueadoHastaMs = relojMs.getAsLong() + duracionBloqueoMs;
        }
    }

    public synchronized void registrarExito(String clave) {
        registros.remove(clave);
    }

    private static final class Registro {
        private int fallos;
        private long bloqueadoHastaMs;
    }
}
