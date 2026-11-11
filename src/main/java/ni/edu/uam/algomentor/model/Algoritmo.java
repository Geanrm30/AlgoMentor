package ni.edu.uam.algomentor.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "algoritmo")
public class Algoritmo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String categoria; // ORDENAMIENTO o MATEMATICO

    @Column(length = 30)
    private String complejidad;

    @Column(length = 500)
    private String descripcion;

    @OneToMany(mappedBy = "algoritmo")
    private List<Ejercicio> ejercicios = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getComplejidad() { return complejidad; }
    public void setComplejidad(String complejidad) { this.complejidad = complejidad; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public List<Ejercicio> getEjercicios() { return ejercicios; }
    public void setEjercicios(List<Ejercicio> ejercicios) { this.ejercicios = ejercicios; }
}