package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.engine.GeneradorEjercicios;
import ni.edu.uam.algomentor.engine.Paso;
import ni.edu.uam.algomentor.engine.TipoOperacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Desafío cronometrado: cada pregunta pide el estado del arreglo después de aplicar cierto número de
 * cambios (intercambios o escrituras) de un algoritmo. Lleva el puntaje y la racha de la partida.
 */
public final class DesafioService {

    public static final int SEGUNDOS = 60;
    private static final Pattern ENTERO = Pattern.compile("-?\\d+");

    public record Pregunta(String enunciado, String esperada) { }

    private final Random azar;
    private int puntos;
    private int aciertos;
    private int errores;
    private int racha;
    private int mejorRacha;

    public DesafioService(long semilla) {
        this.azar = new Random(semilla);
    }

    public DesafioService() {
        this(System.nanoTime());
    }

    /** Genera una pregunta; con más rondas los arreglos crecen hasta siete elementos. */
    public Pregunta nuevaPregunta(int ronda) {
        List<CatalogoAlgoritmos> algoritmos = CatalogoAlgoritmos.ordenamientos();
        while (true) {
            CatalogoAlgoritmos algoritmo = algoritmos.get(azar.nextInt(algoritmos.size()));
            int tamano = 4 + Math.min(Math.max(ronda, 0) / 3, 3);
            int[] datos = new GeneradorEjercicios(azar.nextLong()).arregloAleatorio(tamano, -9, 30);
            List<Paso> cambios = algoritmo.crearOrdenamiento().ejecutar(datos).pasos().stream()
                    .filter(p -> p.tipo() == TipoOperacion.INTERCAMBIAR || p.tipo() == TipoOperacion.ESCRIBIR)
                    .toList();
            if (cambios.isEmpty()) {
                continue;
            }
            int numero = 1 + azar.nextInt(Math.min(3, cambios.size()));
            return new Pregunta("Aplica " + algoritmo.etiqueta() + " a " + Arrays.toString(datos)
                    + ".\n¿Cómo queda el arreglo después del cambio número " + numero
                    + "? (Un cambio es un intercambio o una escritura; las comparaciones no cuentan.)",
                    cambios.get(numero - 1).estadoPosterior());
        }
    }

    /** Compara solo los números, sin importar corchetes, comas ni espacios. */
    public static boolean coincide(String esperada, String respuesta) {
        List<Integer> a = enteros(esperada);
        List<Integer> b = enteros(respuesta);
        return !b.isEmpty() && a.equals(b);
    }

    private static List<Integer> enteros(String texto) {
        List<Integer> valores = new ArrayList<>();
        if (texto == null) {
            return valores;
        }
        Matcher m = ENTERO.matcher(texto);
        while (m.find()) {
            try {
                valores.add(Integer.parseInt(m.group()));
            } catch (NumberFormatException e) {
                return new ArrayList<>();
            }
        }
        return valores;
    }

    /** Un acierto vale 10 puntos más 2 por cada acierto seguido previo (hasta 5). */
    public boolean responder(Pregunta pregunta, String respuesta) {
        boolean correcta = coincide(pregunta.esperada(), respuesta);
        if (correcta) {
            puntos += 10 + 2 * Math.min(racha, 5);
            aciertos++;
            racha++;
            mejorRacha = Math.max(mejorRacha, racha);
        } else {
            errores++;
            racha = 0;
        }
        return correcta;
    }

    public int puntos() { return puntos; }
    public int aciertos() { return aciertos; }
    public int errores() { return errores; }
    public int racha() { return racha; }
    public int mejorRacha() { return mejorRacha; }
}
