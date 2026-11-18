package ni.edu.uam.algomentor.util;

import ni.edu.uam.algomentor.engine.FormatoDatos;
import ni.edu.uam.algomentor.engine.Fraccion;

public final class EntradaAprendizaje {

    private EntradaAprendizaje() { }

    public static int[] leerArregloGuardado(String texto) {
        int[] datos = texto != null && texto.strip().startsWith(FormatoDatos.VERSION + ";")
                ? FormatoDatos.leerArreglo(texto) : leerArreglo(texto);
        if (datos.length > FormatoDatos.MAX_ELEMENTOS_VISUALES) {
            throw new IllegalArgumentException("Para visualizar, usa como máximo " + FormatoDatos.MAX_ELEMENTOS_VISUALES + " números.");
        }
        return datos;
    }

    public static Fraccion[][] leerMatrizGuardada(String texto) {
        return texto != null && texto.strip().startsWith(FormatoDatos.VERSION + ";")
                ? FormatoDatos.leerMatriz(texto) : leerMatriz(texto);
    }

    public static int[] leerArreglo(String texto) {
        int[] valores = leerArregloSinLimite(texto);
        if (valores.length > FormatoDatos.MAX_ELEMENTOS_VISUALES) {
            throw new IllegalArgumentException("Para visualizar, usa como máximo " + FormatoDatos.MAX_ELEMENTOS_VISUALES + " números.");
        }
        return valores;
    }

    public static int[] leerArregloSinLimite(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Ingresa al menos un número entero.");
        }
        String[] partes = texto.strip().split(",", -1);
        int[] valores = new int[partes.length];
        for (int i = 0; i < partes.length; i++) {
            try {
                valores[i] = Integer.parseInt(partes[i].strip());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Separa números enteros con comas; revisa el valor " + (i + 1) + ".", e);
            }
        }
        return valores;
    }

    public static Fraccion[][] leerMatriz(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Ingresa una matriz ampliada.");
        }
        String[] filas = texto.strip().split("\\R", -1);
        if (filas.length > FormatoDatos.MAX_FILAS) {
            throw new IllegalArgumentException("La matriz admite como máximo 6 filas.");
        }
        Fraccion[][] matriz = new Fraccion[filas.length][];
        for (int i = 0; i < filas.length; i++) {
            String[] celdas = filas[i].split(",", -1);
            matriz[i] = new Fraccion[celdas.length];
            for (int j = 0; j < celdas.length; j++) {
                try {
                    matriz[i][j] = Fraccion.leer(celdas[j].strip());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Revisa fila " + (i + 1) + ", columna " + (j + 1) + ": " + e.getMessage(), e);
                }
            }
        }
        FormatoDatos.validarMatriz(matriz);
        return matriz;
    }
}
