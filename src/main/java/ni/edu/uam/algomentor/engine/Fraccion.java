package ni.edu.uam.algomentor.engine;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Fraccion exacta con BigInteger. Siempre queda reducida y con denominador positivo,
 * asi Gauss y Gauss-Jordan no pierden precision por redondeo.
 */
public final class Fraccion implements Comparable<Fraccion> {

    public static final Fraccion CERO = new Fraccion(BigInteger.ZERO, BigInteger.ONE);
    public static final Fraccion UNO = new Fraccion(BigInteger.ONE, BigInteger.ONE);

    private final BigInteger numerador;
    private final BigInteger denominador;

    private Fraccion(BigInteger numerador, BigInteger denominador) {
        this.numerador = numerador;
        this.denominador = denominador;
    }

    public static Fraccion de(long entero) {
        return new Fraccion(BigInteger.valueOf(entero), BigInteger.ONE);
    }

    public static Fraccion de(long numerador, long denominador) {
        return de(BigInteger.valueOf(numerador), BigInteger.valueOf(denominador));
    }

    public static Fraccion de(BigInteger numerador, BigInteger denominador) {
        if (denominador.signum() == 0) {
            throw new ArithmeticException("El denominador no puede ser cero.");
        }
        if (denominador.signum() < 0) {
            numerador = numerador.negate();
            denominador = denominador.negate();
        }
        BigInteger mcd = numerador.gcd(denominador);
        return new Fraccion(numerador.divide(mcd), denominador.divide(mcd));
    }

    /** Lee "3/4", "-2" o " 5/10 ". */
    public static Fraccion leer(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("La fraccion esta vacia.");
        }
        String limpio = texto.strip();
        try {
            int barra = limpio.indexOf('/');
            if (barra < 0) {
                return de(new BigInteger(limpio), BigInteger.ONE);
            }
            BigInteger num = new BigInteger(limpio.substring(0, barra).strip());
            BigInteger den = new BigInteger(limpio.substring(barra + 1).strip());
            if (den.signum() == 0) {
                throw new IllegalArgumentException("El denominador no puede ser cero.");
            }
            return de(num, den);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Numero o fraccion no valida: '" + limpio + "'.", e);
        }
    }

    public Fraccion sumar(Fraccion otra) {
        return de(numerador.multiply(otra.denominador).add(otra.numerador.multiply(denominador)),
                denominador.multiply(otra.denominador));
    }

    public Fraccion restar(Fraccion otra) {
        return de(numerador.multiply(otra.denominador).subtract(otra.numerador.multiply(denominador)),
                denominador.multiply(otra.denominador));
    }

    public Fraccion multiplicar(Fraccion otra) {
        return de(numerador.multiply(otra.numerador), denominador.multiply(otra.denominador));
    }

    public Fraccion dividir(Fraccion otra) {
        if (otra.esCero()) {
            throw new ArithmeticException("No se puede dividir entre cero.");
        }
        return de(numerador.multiply(otra.denominador), denominador.multiply(otra.numerador));
    }

    public Fraccion negar() {
        return new Fraccion(numerador.negate(), denominador);
    }

    public Fraccion reciproco() {
        if (esCero()) {
            throw new ArithmeticException("El cero no tiene reciproco.");
        }
        return de(denominador, numerador);
    }

    public boolean esCero() {
        return numerador.signum() == 0;
    }

    public boolean esEntero() {
        return denominador.equals(BigInteger.ONE);
    }

    public int signo() {
        return numerador.signum();
    }

    public BigInteger numerador() {
        return numerador;
    }

    public BigInteger denominador() {
        return denominador;
    }

    @Override
    public int compareTo(Fraccion otra) {
        return numerador.multiply(otra.denominador).compareTo(otra.numerador.multiply(denominador));
    }

    @Override
    public boolean equals(Object objeto) {
        return objeto instanceof Fraccion otra
                && numerador.equals(otra.numerador)
                && denominador.equals(otra.denominador);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numerador, denominador);
    }

    @Override
    public String toString() {
        return esEntero() ? numerador.toString() : numerador + "/" + denominador;
    }
}
