package ni.edu.uam.algomentor.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "habilidad")
public class Habilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @OneToMany(mappedBy = "habilidad")
    private List<ErrorDetectado> errores = new ArrayList<>();

    @OneToMany(mappedBy = "habilidad")
    private List<ProgresoHabilidad> progresos = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public List<ErrorDetectado> getErrores() { return errores; }
    public void setErrores(List<ErrorDetectado> errores) { this.errores = errores; }
    public List<ProgresoHabilidad> getProgresos() { return progresos; }
    public void setProgresos(List<ProgresoHabilidad> progresos) { this.progresos = progresos; }
}