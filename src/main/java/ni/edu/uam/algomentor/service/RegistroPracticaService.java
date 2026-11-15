package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.ErrorOperacion;
import ni.edu.uam.algomentor.engine.TipoError;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class RegistroPracticaService {

    public void registrar(Long perfilId, Long ejercicioId, String respuesta, boolean correcto,
                          int duracionSegundos, List<ErrorOperacion> errores) {
        HabilidadErrorResolver resolver = new HabilidadErrorResolver();
        IntentoService.PasoRegistrado paso = construirPaso(respuesta, correcto, errores, resolver::resolverId);
        new IntentoService().registrarIntento(perfilId, ejercicioId, "PRACTICA", correcto, duracionSegundos,
                List.of(paso));
    }

    static IntentoService.PasoRegistrado construirPaso(String respuesta, boolean correcto,
                                                        List<ErrorOperacion> errores,
                                                        Function<TipoError, Long> resolverId) {
        Objects.requireNonNull(errores, "La lista de errores es obligatoria.");
        Objects.requireNonNull(resolverId, "El resolvedor de habilidades es obligatorio.");
        if (correcto && !errores.isEmpty()) {
            throw new IllegalArgumentException("Un paso correcto no puede tener errores.");
        }
        Map<TipoError, Long> ids = new EnumMap<>(TipoError.class);
        Function<TipoError, Long> id = tipo -> ids.computeIfAbsent(tipo, clave -> {
            try {
                return resolverId.apply(clave);
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Falta la habilidad "
                        + HabilidadErrorResolver.nombreHabilidad(clave)
                        + ". Carga schema.sql y vuelve a intentar.", e);
            }
        });
        List<IntentoService.ErrorRegistrado> registrados = new ArrayList<>();
        for (ErrorOperacion error : errores) {
            registrados.add(new IntentoService.ErrorRegistrado(id.apply(error.tipo()),
                    error.tipo(), error.descripcion()));
        }
        TipoError habilidadPrincipal = errores.isEmpty() ? TipoError.OPERACION_INVALIDA : errores.getFirst().tipo();
        return new IntentoService.PasoRegistrado(1, respuesta, correcto, id.apply(habilidadPrincipal), registrados);
    }
}
