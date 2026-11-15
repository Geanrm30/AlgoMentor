package ni.edu.uam.algomentor.engine;

/** Error concreto del estudiante. fila y columna son base 0; -1 si el error no es de una celda. */
public record ErrorOperacion(TipoError tipo, int fila, int columna, String descripcion) { }
