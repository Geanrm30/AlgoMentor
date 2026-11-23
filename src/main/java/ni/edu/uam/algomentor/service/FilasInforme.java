package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.util.TextoInterfaz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Convierte las estadísticas de un perfil en filas genéricas (sección, cabeceras y cinco columnas de
 * texto) que la plantilla del informe agrupa por sección. Cada sección siempre aporta al menos una fila
 * para que el informe muestre su cabecera aunque todavía no haya datos.
 */
final class FilasInforme {

    static final int INTENTOS_EN_INFORME = 15;

    private FilasInforme() { }

    static List<Map<String, ?>> construir(
            List<EstadisticasDAO.EstadisticaHabilidad> habilidades,
            List<EstadisticasDAO.RendimientoAlgoritmo> algoritmos,
            List<EstadisticasDAO.ErrorPorTipo> errores,
            List<EstadisticasDAO.IntentoReciente> recientes) {
        List<Map<String, ?>> filas = new ArrayList<>();

        String[] cabecerasHabilidad = {"HABILIDAD", "ACIERTOS", "ERRORES", "EVALUACIONES", "ACIERTOS %"};
        if (habilidades.isEmpty()) {
            filas.add(vacia("1", "Resultados por habilidad", cabecerasHabilidad,
                    "Sin progreso por habilidad todavía."));
        }
        for (EstadisticasDAO.EstadisticaHabilidad h : habilidades) {
            filas.add(fila("1", "Resultados por habilidad", cabecerasHabilidad,
                    h.nombre(), Integer.toString(h.aciertos()), Integer.toString(h.errores()),
                    Long.toString(h.totalEvaluaciones()), porcentaje(h.porcentajeAciertos())));
        }

        String[] cabecerasAlgoritmo = {"ALGORITMO", "INTENTOS", "CORRECTOS", "INCORRECTOS", "ACIERTOS %"};
        if (algoritmos.isEmpty()) {
            filas.add(vacia("2", "Resultados por algoritmo", cabecerasAlgoritmo,
                    "Sin intentos con algoritmos todavía."));
        }
        for (EstadisticasDAO.RendimientoAlgoritmo a : algoritmos) {
            filas.add(fila("2", "Resultados por algoritmo", cabecerasAlgoritmo,
                    TextoInterfaz.algoritmo(a.algoritmo()), Long.toString(a.intentos()),
                    Long.toString(a.correctos()), Long.toString(a.incorrectos()),
                    porcentaje(a.porcentajeAciertos())));
        }

        String[] cabecerasError = {"TIPO DE ERROR", "CANTIDAD", "% DEL TOTAL", "", ""};
        long totalErrores = errores.stream().mapToLong(EstadisticasDAO.ErrorPorTipo::cantidad).sum();
        if (errores.isEmpty()) {
            filas.add(vacia("3", "Errores por tipo", cabecerasError, "Sin errores registrados."));
        }
        for (EstadisticasDAO.ErrorPorTipo e : errores) {
            double parte = totalErrores == 0 ? 0 : e.cantidad() * 100.0 / totalErrores;
            filas.add(fila("3", "Errores por tipo", cabecerasError,
                    TextoInterfaz.tipoError(e.tipo()), Long.toString(e.cantidad()), porcentaje(parte), "", ""));
        }

        String[] cabecerasHistorial = {"ALGORITMO", "FECHA", "MODO", "RESULTADO", "DURACIÓN"};
        String tituloHistorial = "Últimos intentos";
        List<EstadisticasDAO.IntentoReciente> ultimos =
                recientes.subList(0, Math.min(INTENTOS_EN_INFORME, recientes.size()));
        if (ultimos.isEmpty()) {
            filas.add(vacia("4", tituloHistorial, cabecerasHistorial, "Sin intentos registrados."));
        }
        for (EstadisticasDAO.IntentoReciente i : ultimos) {
            filas.add(fila("4", tituloHistorial, cabecerasHistorial,
                    TextoInterfaz.algoritmo(i.algoritmo()), TextoInterfaz.fecha(i.fecha()),
                    TextoInterfaz.modo(i.modo()), TextoInterfaz.resultado(i.correcto()),
                    i.duracionSegundos() == null ? "—" : i.duracionSegundos() + " s"));
        }
        return filas;
    }

    static String porcentaje(double valor) {
        return String.format(Locale.ROOT, "%.1f%%", valor);
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
