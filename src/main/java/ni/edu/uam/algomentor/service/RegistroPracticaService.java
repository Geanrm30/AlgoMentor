package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.BaseDAO;
import ni.edu.uam.algomentor.model.Habilidad;

import java.util.List;

public class RegistroPracticaService {

    public void registrar(Long perfilId, Long ejercicioId, String respuesta, boolean correcto, int duracionSegundos) {
        Long habilidadId = new BaseDAO<>(Habilidad.class).listar().stream()
                .filter(h -> "Procedimiento".equals(h.getNombre()))
                .map(Habilidad::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Falta la habilidad Procedimiento en la base de datos."));
        new IntentoService().registrarIntento(perfilId, ejercicioId, "PRACTICA", correcto, duracionSegundos,
                List.of(new IntentoService.PasoRegistrado(1, respuesta, correcto, habilidadId, List.of())));
    }
}
