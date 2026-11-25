package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.CatalogoDAO;
import ni.edu.uam.algomentor.util.TextoInterfaz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filas genéricas (sección, cabeceras y cinco columnas de texto) del informe del catálogo. Cada sección
 * aporta al menos una fila para que se vea su cabecera aunque todavía no haya datos.
 */
final class FilasCatalogo {

    static final int EJERCICIOS_EN_INFORME = 10;

    private FilasCatalogo() { }

    static List<Map<String, ?>> construir(
            List<CatalogoDAO.AlgoritmoCatalogo> algoritmos,
            List<CatalogoDAO.ConteoClave> porDificultad,
            List<CatalogoDAO.ConteoClave> porOrigen,
            List<CatalogoDAO.EjercicioUsado> masUsados) {
        List<Map<String, ?>> filas = new ArrayList<>();

        String[] cabAlgoritmo = {"ALGORITMO", "CATEGORÍA", "EJERCICIOS", "INTENTOS", "ACIERTOS %"};
        String tituloAlgoritmo = "Algoritmos del catálogo";
        if (algoritmos.isEmpty()) {
            filas.add(vacia("1", tituloAlgoritmo, cabAlgoritmo, "No hay algoritmos registrados."));
        }
        for (CatalogoDAO.AlgoritmoCatalogo a : algoritmos) {
            filas.add(fila("1", tituloAlgoritmo, cabAlgoritmo,
                    TextoInterfaz.algoritmo(a.nombre()), TextoInterfaz.categoria(a.categoria()),
                    Long.toString(a.ejercicios()), Long.toString(a.intentos()),
                    a.intentos() == 0 ? "—" : FilasInforme.porcentaje(a.porcentajeAciertos())));
        }

        String[] cabConteo = {"", "CANTIDAD", "% DEL TOTAL", "", ""};
        agregarConteos(filas, "2", "Ejercicios por dificultad", "DIFICULTAD", cabConteo, porDificultad,
                clave -> "Nivel " + clave, "Sin ejercicios registrados.");
        agregarConteos(filas, "3", "Ejercicios por origen", "ORIGEN", cabConteo, porOrigen,
                TextoInterfaz::origen, "Sin ejercicios registrados.");

        String[] cabUsados = {"ALGORITMO", "DIFICULTAD", "ORIGEN", "INTENTOS", "EJERCICIO"};
        String tituloUsados = "Ejercicios más usados";
        List<CatalogoDAO.EjercicioUsado> principales =
                masUsados.subList(0, Math.min(EJERCICIOS_EN_INFORME, masUsados.size()));
        if (principales.isEmpty()) {
            filas.add(vacia("4", tituloUsados, cabUsados, "Sin ejercicios registrados."));
        }
        for (CatalogoDAO.EjercicioUsado e : principales) {
            filas.add(fila("4", tituloUsados, cabUsados,
                    TextoInterfaz.algoritmo(e.algoritmo()), "Nivel " + e.dificultad(),
                    TextoInterfaz.origen(e.origen()), Long.toString(e.intentos()), "#" + e.id()));
        }
        return filas;
    }

    private static void agregarConteos(List<Map<String, ?>> filas, String seccion, String titulo,
                                       String primeraCabecera, String[] cabeceras,
                                       List<CatalogoDAO.ConteoClave> conteos,
                                       java.util.function.UnaryOperator<String> etiqueta, String mensajeVacio) {
        String[] cab = cabeceras.clone();
        cab[0] = primeraCabecera;
        long total = conteos.stream().mapToLong(CatalogoDAO.ConteoClave::cantidad).sum();
        if (conteos.isEmpty()) {
            filas.add(vacia(seccion, titulo, cab, mensajeVacio));
        }
        for (CatalogoDAO.ConteoClave c : conteos) {
            double parte = total == 0 ? 0 : c.cantidad() * 100.0 / total;
            filas.add(fila(seccion, titulo, cab, etiqueta.apply(c.clave()),
                    Long.toString(c.cantidad()), FilasInforme.porcentaje(parte), "", ""));
        }
    }

    private static Map<String, Object> vacia(String seccion, String titulo, String[] cabeceras, String mensaje) {
        return fila(seccion, titulo, cabeceras, mensaje, "", "", "", "");
    }

    private static Map<String, Object> fila(String seccion, String titulo, String[] cabeceras,
                                            String c1, String c2, String c3, String c4, String c5) {
        Map<String, Object> fila = new HashMap<>();
        fila.put("seccion", seccion);
        fila.put("tituloSeccion", titulo);
        for (int i = 0; i < 5; i++) {
            fila.put("h" + (i + 1), cabeceras[i]);
        }
        fila.put("c1", c1);
        fila.put("c2", c2);
        fila.put("c3", c3);
        fila.put("c4", c4);
        fila.put("c5", c5);
        return fila;
    }
}
