package ni.edu.uam.algomentor.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutorizacionServiceTest {

    private final AutorizacionService soloPerfilUno = new AutorizacionService(id -> id == 1L);

    @Test
    void reconoceAlAdministradorYRechazaAlResto() {
        assertTrue(soloPerfilUno.esAdministrador(1L));
        assertFalse(soloPerfilUno.esAdministrador(2L));
    }

    @Test
    void sinPerfilNuncaEsAdministrador() {
        assertFalse(new AutorizacionService(id -> true).esAdministrador(null));
    }

    @Test
    void exigirAdministradorLanzaSecurityExceptionParaUnUsuarioNormal() {
        assertDoesNotThrow(() -> soloPerfilUno.exigirAdministrador(1L));
        assertThrows(SecurityException.class, () -> soloPerfilUno.exigirAdministrador(2L));
        assertThrows(SecurityException.class, () -> soloPerfilUno.exigirAdministrador(null));
    }

    @Test
    void elResumenGlobalSeNiegaAUnUsuarioNormalAntesDeConsultarDatos() {
        ResumenAdministracionService servicio =
                new ResumenAdministracionService(new AutorizacionService(id -> false));
        assertThrows(SecurityException.class, () -> servicio.cargar(2L));
    }
}
