package escrims.observer;

import escrims.dominio.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PATRON OBSERVER - Interfaz del evento de dominio.
 * Todos los eventos que se publican en el DomainEventBus deben implementar esta interfaz.
 */
public interface DomainEvent {
    UUID getScrimId();
    TipoEvento getTipo();
    LocalDateTime getFecha();
}
