package escrims.infra.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alertas_busqueda")
public class AlertaBusquedaJpaEntity {

    @Id
    private UUID id;

    private UUID busquedaId;
    private UUID usuarioId;
    private String username;
    private String email;
    private String regionUsuario;
    private String passwordHash;
    private String rolSistema;
    private UUID scrimId;
    private String mensaje;
    private LocalDateTime fechaCreacion;

    protected AlertaBusquedaJpaEntity() {
    }

    public AlertaBusquedaJpaEntity(UUID id,
                                   UUID busquedaId,
                                   UUID usuarioId,
                                   String username,
                                   String email,
                                   String regionUsuario,
                                   String passwordHash,
                                   String rolSistema,
                                   UUID scrimId,
                                   String mensaje,
                                   LocalDateTime fechaCreacion) {
        this.id = id;
        this.busquedaId = busquedaId;
        this.usuarioId = usuarioId;
        this.username = username;
        this.email = email;
        this.regionUsuario = regionUsuario;
        this.passwordHash = passwordHash;
        this.rolSistema = rolSistema;
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

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRegionUsuario() {
        return regionUsuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRolSistema() {
        return rolSistema;
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
