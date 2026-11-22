package ni.edu.uam.algomentor.util;

import ni.edu.uam.algomentor.model.Perfil;

public final class SesionUsuario {

    private static Perfil perfil;

    private SesionUsuario() { }

    public static synchronized void iniciar(Perfil perfilAutenticado) {
        if (perfilAutenticado == null || perfilAutenticado.getId() == null) {
            throw new IllegalArgumentException("Se necesita un perfil autenticado.");
        }
        perfil = perfilAutenticado;
    }

    public static synchronized Perfil obtenerPerfil() {
        if (perfil == null) {
            throw new IllegalStateException("No hay una sesión activa.");
        }
        return perfil;
    }

    public static synchronized void cerrar() {
        perfil = null;
    }
}
