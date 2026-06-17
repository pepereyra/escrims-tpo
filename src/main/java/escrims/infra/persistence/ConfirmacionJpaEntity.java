package escrims.infra.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "confirmaciones")
public class ConfirmacionJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scrim_id", nullable = false)
    private ScrimJpaEntity scrim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioJpaEntity usuario;

    private boolean confirmado;

    private LocalDateTime fechaConfirmacion;

    protected ConfirmacionJpaEntity() {
    }

    public ConfirmacionJpaEntity(UUID id,
                                 UsuarioJpaEntity usuario,
                                 boolean confirmado,
                                 LocalDateTime fechaConfirmacion) {
        this.id = id;
        this.usuario = usuario;
        this.confirmado = confirmado;
        this.fechaConfirmacion = fechaConfirmacion;
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

    public boolean isConfirmado() {
        return confirmado;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }
}
