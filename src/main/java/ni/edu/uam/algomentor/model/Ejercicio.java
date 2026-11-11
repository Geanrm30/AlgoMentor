package ni.edu.uam.algomentor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ejercicio")
public class Ejercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "algoritmo_id", nullable = false)
    private Algoritmo algoritmo;

    @Column(nullable = false, columnDefinition = "text")
    private String datos;

    @Column(nullable = false)
    private Integer dificultad;

    @Column(nullable = false, length = 20)
    private String origen; // ALEATORIO, MANUAL o PERSONALIZADO

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToMany(mappedBy = "ejercicio")
    private List<Intento> intentos = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Algoritmo getAlgoritmo() { return algoritmo; }
    public void setAlgoritmo(Algoritmo algoritmo) { this.algoritmo = algoritmo; }
    public String getDatos() { return datos; }
    public void setDatos(String datos) { this.datos = datos; }
    public Integer getDificultad() { return dificultad; }
    public void setDificultad(Integer dificultad) { this.dificultad = dificultad; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public List<Intento> getIntentos() { return intentos; }
    public void setIntentos(List<Intento> intentos) { this.intentos = intentos; }
}