package ni.edu.uam.algomentor.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutenticacionServiceTest {

    @Test
    void normalizaUsuarioIgnorandoEspaciosYMayusculas() {
        assertEquals("ana_01", AutenticacionService.validarUsuario(" Ana_01 "));
    }

    @Test
    void rechazaUsuarioConFormatoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> AutenticacionService.validarUsuario("ab"));
        assertThrows(IllegalArgumentException.class,
                () -> AutenticacionService.validarUsuario("usuario con espacios"));
    }

    @Test
    void protegeYVerificaContrasenaConHashSalado() {
        char[] contrasena = "ClaveSegura123".toCharArray();

        String hash = AutenticacionService.hashContrasena(contrasena);

        assertNotEquals(new String(contrasena), hash);
        assertTrue(AutenticacionService.verificarContrasena(contrasena, hash));
        assertFalse(AutenticacionService.verificarContrasena(
                "OtraClave123".toCharArray(), hash));
        assertNotEquals(hash, AutenticacionService.hashContrasena(contrasena));
    }

    @Test
    void requiereContrasenaDeOchoCaracteres() {
        assertThrows(IllegalArgumentException.class,
                () -> AutenticacionService.validarContrasena("corta".toCharArray()));
        assertDoesNotThrow(() ->
                AutenticacionService.validarContrasena("ClaveSegura123".toCharArray()));
    }
}
