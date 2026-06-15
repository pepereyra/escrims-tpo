package escrims.infra.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "busquedas_favoritas")
public class BusquedaFavoritaJpaEntity {

    @Id
    private UUID id;

    private UUID usuarioId;
    private String username;
    private String email;
    private String regionUsuario;
    private String passwordHash;
    private String rolSistema;
    private String juego;
    private String formato;
    private String region;
    private Integer rangoMin;
    private Integer rangoMax;
    private Integer latenciaMax;
    private LocalDate fecha;
    private LocalDateTime fechaCreacion;

    protected BusquedaFavoritaJpaEntity() {
    }

    public BusquedaFavoritaJpaEntity(UUID id,
                                     UUID usuarioId,
                                     String username,
                                     String email,
                                     String regionUsuario,
                                     String passwordHash,
                                     String rolSistema,
                                     String juego,
                                     String formato,
                                     String region,
                                     Integer rangoMin,
                                     Integer rangoMax,
                                     Integer latenciaMax,
                                     LocalDate fecha,
                                     LocalDateTime fechaCreacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.username = username;
        this.email = email;
        this.regionUsuario = regionUsuario;
        this.passwordHash = passwordHash;
        this.rolSistema = rolSistema;
        this.juego = juego;
        this.formato = formato;
        this.region = region;
        this.rangoMin = rangoMin;
        this.rangoMax = rangoMax;
        this.latenciaMax = latenciaMax;
        this.fecha = fecha;
        this.fechaCreacion = fechaCreacion;
    }

    public UUID getId() {
        return id;
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

    public String getJuego() {
        return juego;
    }

    public String getFormato() {
        return formato;
    }

    public String getRegion() {
        return region;
    }

    public Integer getRangoMin() {
        return rangoMin;
    }

    public Integer getRangoMax() {
        return rangoMax;
    }

    public Integer getLatenciaMax() {
        return latenciaMax;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
