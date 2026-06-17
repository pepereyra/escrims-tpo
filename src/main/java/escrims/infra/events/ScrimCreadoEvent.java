package escrims.infra.events;

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
    private final int rangoMin;
    private final int rangoMax;
    private final int latenciaMax;
    private final LocalDateTime fechaHora;
    private final LocalDateTime fecha;

    public ScrimCreadoEvent(UUID scrimId, String juego, String region,
                             String formato, int cuposTotales) {
        this(scrimId, juego, region, formato, cuposTotales, 0, 0, 0, LocalDateTime.now());
    }

    public ScrimCreadoEvent(UUID scrimId,
                            String juego,
                            String region,
                            String formato,
                            int cuposTotales,
                            int rangoMin,
                            int rangoMax,
                            int latenciaMax,
                            LocalDateTime fechaHora) {
        this.scrimId = scrimId;
        this.juego = juego;
        this.region = region;
        this.formato = formato;
        this.cuposTotales = cuposTotales;
        this.rangoMin = rangoMin;
        this.rangoMax = rangoMax;
        this.latenciaMax = latenciaMax;
        this.fechaHora = fechaHora;
        this.fecha = LocalDateTime.now();
    }

    @Override
    public UUID getScrimId() {
        return scrimId;
    }

    @Override
    public String getTipo() {
        return "SCRIM_CREADO";
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

    public int getRangoMin() {
        return rangoMin;
    }

    public int getRangoMax() {
        return rangoMax;
    }

    public int getLatenciaMax() {
        return latenciaMax;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    @Override
    public String toString() {
        return "ScrimCreadoEvent{scrimId=" + scrimId +
                ", juego='" + juego + '\'' +
                ", region='" + region + '\'' +
                ", formato='" + formato + '\'' +
                ", cuposTotales=" + cuposTotales +
                ", rangoMin=" + rangoMin +
                ", rangoMax=" + rangoMax +
                ", latenciaMax=" + latenciaMax +
                ", fechaHora=" + fechaHora +
                ", fecha=" + fecha + '}';
    }
}
