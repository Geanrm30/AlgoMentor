package ni.edu.uam.algomentor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToMany(mappedBy = "perfil")
    private List<Intento> intentos = new ArrayList<>();

    @OneToMany(mappedBy = "perfil")
    private List<ProgresoHabilidad> progresos = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public List<Intento> getIntentos() { return intentos; }
    public void setIntentos(List<Intento> intentos) { this.intentos = intentos; }
    public List<ProgresoHabilidad> getProgresos() { return progresos; }
    public void setProgresos(List<ProgresoHabilidad> progresos) { this.progresos = progresos; }
}