package escrims.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "estadisticas")
public class EstadisticaJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scrim_id", nullable = false)
    private ScrimJpaEntity scrim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioJpaEntity usuario;

    private boolean mvp;
    private int kills;
    private int deaths;
    private int assists;

    @Column(nullable = false)
    private String observaciones;

    protected EstadisticaJpaEntity() {
    }

    public EstadisticaJpaEntity(UUID id,
                                UsuarioJpaEntity usuario,
                                boolean mvp,
                                int kills,
                                int deaths,
                                int assists,
                                String observaciones) {
        this.id = id;
        this.usuario = usuario;
        this.mvp = mvp;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.observaciones = observaciones == null ? "" : observaciones;
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

    public boolean isMvp() {
        return mvp;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getAssists() {
        return assists;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
