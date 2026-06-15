package escrims.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReporteConducta {

    private final UUID id;
    private final UUID scrimId;
    private final Usuario reportante;
    private final Usuario reportado;
    private final String motivo;
    private EstadoModeracion estado;
    private String sancion;
    private String etapaResolucion;
    private final LocalDateTime fechaCreacion;

    public ReporteConducta(UUID scrimId, Usuario reportante, Usuario reportado, String motivo) {
        this(UUID.randomUUID(), scrimId, reportante, reportado, motivo, "PENDIENTE", "", "SIN_PROCESAR", LocalDateTime.now());
    }

    public ReporteConducta(UUID id,
                           UUID scrimId,
                           Usuario reportante,
                           Usuario reportado,
                           String motivo,
                           String estado,
                           String sancion,
                           LocalDateTime fechaCreacion) {
        this(id, scrimId, reportante, reportado, motivo, estado, sancion, "SIN_PROCESAR", fechaCreacion);
    }

    public ReporteConducta(UUID id,
                           UUID scrimId,
                           Usuario reportante,
                           Usuario reportado,
                           String motivo,
                           String estado,
                           String sancion,
                           String etapaResolucion,
                           LocalDateTime fechaCreacion) {
        if (reportante.getId().equals(reportado.getId())) {
            throw new IllegalArgumentException("Un usuario no puede reportarse a si mismo.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del reporte es obligatorio.");
        }

        this.id = id;
        this.scrimId = scrimId;
        this.reportante = reportante;
        this.reportado = reportado;
        this.motivo = motivo;
        this.estado = crearEstado(estado);
        this.sancion = sancion == null ? "" : sancion;
        this.etapaResolucion = etapaResolucion == null || etapaResolucion.isBlank() ? "SIN_PROCESAR" : etapaResolucion;
        this.fechaCreacion = fechaCreacion == null ? LocalDateTime.now() : fechaCreacion;
    }

    public void aprobar(String sancion) {
        this.estado = new AprobadoModeracion();
        this.sancion = sancion == null ? "" : sancion;
        this.reportado.agregarStrike();
    }

    public void rechazar() {
        this.estado = new RechazadoModeracion();
        this.sancion = "";
    }

    public void marcarEtapaResolucion(String etapaResolucion) {
        this.etapaResolucion = etapaResolucion == null || etapaResolucion.isBlank()
                ? "SIN_PROCESAR"
                : etapaResolucion;
    }

    public UUID getId() {
        return id;
    }

    public UUID getScrimId() {
        return scrimId;
    }

    public Usuario getReportante() {
        return reportante;
    }

    public Usuario getReportado() {
        return reportado;
    }

    public String getMotivo() {
        return motivo;
    }

    public EstadoModeracion getEstado() {
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

    private EstadoModeracion crearEstado(String estado) {
        if ("APROBADO".equals(estado)) {
            return new AprobadoModeracion();
        }
        if ("RECHAZADO".equals(estado)) {
            return new RechazadoModeracion();
        }
        return new PendienteModeracion();
    }
}
