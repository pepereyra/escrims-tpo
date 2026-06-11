package escrims.observer;

import escrims.dominio.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PATRON OBSERVER - Evento de dominio: Scrim creado.
 *
 * Se publica cuando un nuevo ScrimContext es creado exitosamente.
 * Permite que los suscriptores reaccionen al nacimiento de un scrim
 * (ej: notificar a jugadores de la región, registrar en logs, etc.).
 *
 * GRASP Information Expert: conoce los datos del scrim recién creado.
 * GRASP Creator: creado por ScrimContext en su constructor.
 */
public class ScrimCreadoEvent implements DomainEvent {

    private final UUID scrimId;
    private final String juego;
    private final String region;
    private final String formato;
    private final int cuposTotales;
    private final LocalDateTime fecha;

    public ScrimCreadoEvent(UUID scrimId, String juego, String region,
                             String formato, int cuposTotales) {
        this.scrimId = scrimId;
        this.juego = juego;
        this.region = region;
        this.formato = formato;
        this.cuposTotales = cuposTotales;
        this.fecha = LocalDateTime.now();
    }

    @Override
    public UUID getScrimId() {
        return scrimId;
    }

    @Override
    public TipoEvento getTipo() {
        return TipoEvento.SCRIM_CREADO;
    }

    @Override
    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getJuego() {
        return juego;
    }

    public String getRegion() {
        return region;
    }

    public String getFormato() {
        return formato;
    }

    public int getCuposTotales() {
        return cuposTotales;
    }

    @Override
    public String toString() {
        return "ScrimCreadoEvent{scrimId=" + scrimId +
                ", juego='" + juego + '\'' +
                ", region='" + region + '\'' +
                ", formato='" + formato + '\'' +
                ", cuposTotales=" + cuposTotales +
                ", fecha=" + fecha + '}';
    }
}
