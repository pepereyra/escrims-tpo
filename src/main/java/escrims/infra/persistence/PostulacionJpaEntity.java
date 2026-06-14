package escrims.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "postulaciones")
public class PostulacionJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scrim_id", nullable = false)
    private ScrimJpaEntity scrim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioJpaEntity usuario;

    @Column(nullable = false)
    private String rolDeseado;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private LocalDateTime fechaPostulacion;

    protected PostulacionJpaEntity() {
    }

    public PostulacionJpaEntity(UUID id,
                                UsuarioJpaEntity usuario,
                                String rolDeseado,
                                String estado,
                                LocalDateTime fechaPostulacion) {
        this.id = id;
        this.usuario = usuario;
        this.rolDeseado = rolDeseado;
        this.estado = estado;
        this.fechaPostulacion = fechaPostulacion;
    }

    void setScrim(ScrimJpaEntity scrim) {
        this.scrim = scrim;
    }

    public UUID getId() {
        return id;
    }

    public UsuarioJpaEntity getUsuario() {
        return usuario;
    }

    public String getRolDeseado() {
        return rolDeseado;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFechaPostulacion() {
        return fechaPostulacion;
    }
}
