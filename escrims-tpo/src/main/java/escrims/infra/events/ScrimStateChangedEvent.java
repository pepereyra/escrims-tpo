package escrims.infra.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PATRON OBSERVER - Evento concreto de cambio de estado del Scrim.
 */
public class ScrimStateChangedEvent implements DomainEvent {

    private final UUID scrimId;
    private final String estadoAnterior;
    private final String nuevoEstado;
    private final LocalDateTime fecha;

    public ScrimStateChangedEvent(UUID scrimId, String estadoAnterior, String nuevoEstado) {
        this.scrimId = scrimId;
        this.estadoAnterior = estadoAnterior;
        this.nuevoEstado = nuevoEstado;
        this.fecha = LocalDateTime.now();
    }

    @Override
    public UUID getScrimId() {
        return scrimId;
    }

    @Override
    public String getTipo() {
        return nuevoEstado;
    }

    @Override
    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public String getNuevoEstado() {
        return nuevoEstado;
    }

    @Override
    public String toString() {
        return "ScrimStateChangedEvent{scrimId=" + scrimId + ", " + estadoAnterior + " -> " + nuevoEstado + "}";
    }
}
