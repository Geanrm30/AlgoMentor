package ni.edu.uam.algomentor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "intento")
public class Intento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    @Column(nullable = false, length = 20)
    private String modo; // SIMULACION o PRACTICA

    private Boolean correcto;

    @Column(name = "duracion_segundos")
    private Integer duracionSegundos;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @OneToMany(mappedBy = "intento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasoIntento> pasos = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public Ejercicio getEjercicio() { return ejercicio; }
    public void setEjercicio(Ejercicio ejercicio) { this.ejercicio = ejercicio; }
    public String getModo() { return modo; }
    public void setModo(String modo) { this.modo = modo; }
    public Boolean getCorrecto() { return correcto; }
    public void setCorrecto(Boolean correcto) { this.correcto = correcto; }
    public Integer getDuracionSegundos() { return duracionSegundos; }
    public void setDuracionSegundos(Integer duracionSegundos) { this.duracionSegundos = duracionSegundos; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public List<PasoIntento> getPasos() { return pasos; }
    public void setPasos(List<PasoIntento> pasos) { this.pasos = pasos; }
}