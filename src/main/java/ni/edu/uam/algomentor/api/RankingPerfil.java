package ni.edu.uam.algomentor.api;

public record RankingPerfil(int posicion, Long perfilId, String nombre, long aciertos,
                            long evaluados, double porcentaje) { }
