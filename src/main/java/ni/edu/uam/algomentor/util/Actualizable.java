package ni.edu.uam.algomentor.util;

/**
 * Vista que puede volver a consultar sus datos sin que el usuario pulse Actualizar. La ventana
 * principal la invoca periódicamente y cuando la aplicación recupera el foco; la vista decide
 * si los datos cambiaron para no redibujar (ni perder la selección) cuando todo sigue igual.
 */
public interface Actualizable {

    void actualizarAutomaticamente();
}
