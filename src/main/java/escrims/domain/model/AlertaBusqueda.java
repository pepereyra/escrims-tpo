package escrims.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class AlertaBusqueda {

    private final UUID id;
    private final UUID busquedaId;
    private final Usuario usuario;
    private final UUID scrimId;
    private final String mensaje;
    private final LocalDateTime fechaCreacion;

    public AlertaBusqueda(UUID busquedaId, Usuario usuario, UUID scrimId, String mensaje) {
        this(UUID.randomUUID(), busquedaId, usuario, scrimId, mensaje, LocalDateTime.now());
    }

    public AlertaBusqueda(UUID id,
                          UUID busquedaId,
                          Usuario usuario,
                          UUID scrimId,
                          String mensaje,
                          LocalDateTime fechaCreacion) {
        this.id = id;
        this.busquedaId = busquedaId;
        this.usuario = usuario;
        this.scrimId = scrimId;
        this.mensaje = mensaje;
        this.fechaCreacion = fechaCreacion;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBusquedaId() {
        return busquedaId;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public UUID getScrimId() {
        return scrimId;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
