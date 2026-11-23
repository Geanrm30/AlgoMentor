package ni.edu.uam.algomentor.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cuenta")
public class Cuenta {

    public static final String ROL_USUARIO = "USUARIO";
    public static final String ROL_ADMIN = "ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String usuario;

    @Column(name = "contrasena_hash", nullable = false, length = 128)
    private String contrasenaHash;

    /** Las cuentas nuevas son siempre USUARIO; el rol ADMIN solo se asigna con el script de la base de datos. */
    @Column(nullable = false, length = 20)
    private String rol = ROL_USUARIO;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfil_id", nullable = false, unique = true)
    private Perfil perfil;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
}
