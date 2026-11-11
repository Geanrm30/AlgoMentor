package ni.edu.uam.algomentor.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "paso_intento")
public class PasoIntento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intento_id", nullable = false)
    private Intento intento;

    @Column(name = "numero_paso", nullable = false)
    private Integer numeroPaso;

    @Column(columnDefinition = "text")
    private String respuesta;

    @Column(nullable = false)
    private Boolean correcto;

    @OneToMany(mappedBy = "pasoIntento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ErrorDetectado> errores = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Intento getIntento() { return intento; }
    public void setIntento(Intento intento) { this.intento = intento; }
    public Integer getNumeroPaso() { return numeroPaso; }
    public void setNumeroPaso(Integer numeroPaso) { this.numeroPaso = numeroPaso; }
    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
    public Boolean getCorrecto() { return correcto; }
    public void setCorrecto(Boolean correcto) { this.correcto = correcto; }
    public List<ErrorDetectado> getErrores() { return errores; }
    public void setErrores(List<ErrorDetectado> errores) { this.errores = errores; }
}