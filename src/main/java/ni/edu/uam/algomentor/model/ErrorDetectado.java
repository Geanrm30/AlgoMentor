package ni.edu.uam.algomentor.model;

import jakarta.persistence.*;

@Entity
@Table(name = "error_detectado")
public class ErrorDetectado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paso_intento_id", nullable = false)
    private PasoIntento pasoIntento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habilidad_id", nullable = false)
    private Habilidad habilidad;

    @Column(name = "tipo_error", nullable = false, length = 30)
    private String tipoError; // SIGNO, FRACCION, ARITMETICO u OPERACION_INVALIDA

    @Column(length = 255)
    private String descripcion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PasoIntento getPasoIntento() { return pasoIntento; }
    public void setPasoIntento(PasoIntento pasoIntento) { this.pasoIntento = pasoIntento; }
    public Habilidad getHabilidad() { return habilidad; }
    public void setHabilidad(Habilidad habilidad) { this.habilidad = habilidad; }
    public String getTipoError() { return tipoError; }
    public void setTipoError(String tipoError) { this.tipoError = tipoError; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}