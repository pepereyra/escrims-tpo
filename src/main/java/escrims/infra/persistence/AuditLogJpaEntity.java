package escrims.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLogJpaEntity {

    @Id
    private UUID id;

    private String actor;
    private String accion;
    private String entidadTipo;
    private String entidadId;

    @Column(length = 1000)
    private String detalle;

    private LocalDateTime fecha;

    protected AuditLogJpaEntity() {
    }

    public AuditLogJpaEntity(UUID id,
                             String actor,
                             String accion,
                             String entidadTipo,
                             String entidadId,
                             String detalle,
                             LocalDateTime fecha) {
        this.id = id;
        this.actor = actor;
        this.accion = accion;
        this.entidadTipo = entidadTipo;
        this.entidadId = entidadId;
        this.detalle = detalle;
        this.fecha = fecha;
    }

    public UUID getId() {
        return id;
    }

    public String getActor() {
        return actor;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidadTipo() {
        return entidadTipo;
    }

    public String getEntidadId() {
        return entidadId;
    }

    public String getDetalle() {
        return detalle;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
