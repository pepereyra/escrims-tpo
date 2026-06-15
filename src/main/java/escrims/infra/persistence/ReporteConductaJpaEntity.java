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
@Table(name = "reportes_conducta")
public class ReporteConductaJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scrim_id", nullable = false)
    private ScrimJpaEntity scrim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reportante_id", nullable = false)
    private UsuarioJpaEntity reportante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reportado_id", nullable = false)
    private UsuarioJpaEntity reportado;

    @Column(nullable = false, length = 1000)
    private String motivo;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String sancion;

    @Column(nullable = false)
    private String etapaResolucion;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    protected ReporteConductaJpaEntity() {
    }

    public ReporteConductaJpaEntity(UUID id,
                                    ScrimJpaEntity scrim,
                                    UsuarioJpaEntity reportante,
                                    UsuarioJpaEntity reportado,
                                    String motivo,
                                    String estado,
                                    String sancion,
                                    LocalDateTime fechaCreacion) {
        this(id, scrim, reportante, reportado, motivo, estado, sancion, "SIN_PROCESAR", fechaCreacion);
    }

    public ReporteConductaJpaEntity(UUID id,
                                    ScrimJpaEntity scrim,
                                    UsuarioJpaEntity reportante,
                                    UsuarioJpaEntity reportado,
                                    String motivo,
                                    String estado,
                                    String sancion,
                                    String etapaResolucion,
                                    LocalDateTime fechaCreacion) {
        this.id = id;
        this.scrim = scrim;
        this.reportante = reportante;
        this.reportado = reportado;
        this.motivo = motivo;
        this.estado = estado;
        this.sancion = sancion;
        this.etapaResolucion = etapaResolucion == null || etapaResolucion.isBlank() ? "SIN_PROCESAR" : etapaResolucion;
        this.fechaCreacion = fechaCreacion;
    }

    public UUID getId() {
        return id;
    }

    public ScrimJpaEntity getScrim() {
        return scrim;
    }

    public UsuarioJpaEntity getReportante() {
        return reportante;
    }

    public UsuarioJpaEntity getReportado() {
        return reportado;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getEstado() {
        return estado;
    }

    public String getSancion() {
        return sancion;
    }

    public String getEtapaResolucion() {
        return etapaResolucion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
