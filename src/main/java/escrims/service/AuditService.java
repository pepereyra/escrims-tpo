package escrims.service;

import escrims.domain.model.AuditLog;
import escrims.infra.events.DomainEvent;
import escrims.infra.events.ScrimStateChangedEvent;
import escrims.infra.events.Subscriber;

import java.util.Collection;

public class AuditService implements Subscriber {

    private final AuditLogRepository auditLogs;

    public AuditService(AuditLogRepository auditLogs) {
        this.auditLogs = auditLogs;
    }

    public AuditLog registrar(String actor,
                              String accion,
                              String entidadTipo,
                              String entidadId,
                              String detalle) {
        AuditLog log = new AuditLog(
                actor == null || actor.isBlank() ? "SYSTEM" : actor,
                accion,
                entidadTipo,
                entidadId,
                detalle
        );
        System.out.println("[AUDIT] " + log.getActor() + " | " + log.getAccion() + " | " + log.getDetalle());
        return auditLogs.save(log);
    }

    public Collection<AuditLog> listar() {
        return auditLogs.findAll();
    }

    @Override
    public void onEvent(DomainEvent evento) {
        if (evento instanceof ScrimStateChangedEvent cambio) {
            registrar(
                    "SYSTEM",
                    "CAMBIO_ESTADO_SCRIM",
                    "SCRIM",
                    cambio.getScrimId().toString(),
                    cambio.getEstadoAnterior() + " -> " + cambio.getNuevoEstado()
            );
            return;
        }

        registrar(
                "SYSTEM",
                evento.getTipo(),
                "SCRIM",
                evento.getScrimId().toString(),
                "Evento de dominio publicado: " + evento.getTipo()
        );
    }
}
