
package ni.edu.uam.algomentor.dao;

import java.util.List;

public interface CRUD<T> {

    void guardar(T entidad);

    T buscarPorId(Long id);

    List<T> listar();

    T actualizar(T entidad);

    void eliminar(Long id);
}
