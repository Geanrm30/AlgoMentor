package ni.edu.uam.algomentor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progreso_habilidad")
public class ProgresoHabilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habilidad_id", nullable = false)
    private Habilidad habilidad;

    @Column(nullable = false)
    private Integer aciertos = 0;

    @Column(nullable = false)
    private Integer errores = 0;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public Habilidad getHabilidad() { return habilidad; }
    public void setHabilidad(Habilidad habilidad) { this.habilidad = habilidad; }
    public Integer getAciertos() { return aciertos; }
    public void setAciertos(Integer aciertos) { this.aciertos = aciertos; }
    public Integer getErrores() { return errores; }
    public void setErrores(Integer errores) { this.errores = errores; }
    public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
}