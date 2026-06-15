package escrims.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLog {

    private final UUID id;
    private final String actor;
    private final String accion;
    private final String entidadTipo;
    private final String entidadId;
    private final String detalle;
    private final LocalDateTime fecha;

    public AuditLog(String actor, String accion, String entidadTipo, String entidadId, String detalle) {
        this(UUID.randomUUID(), actor, accion, entidadTipo, entidadId, detalle, LocalDateTime.now());
    }

    public AuditLog(UUID id,
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
