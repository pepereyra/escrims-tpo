package escrims.domain.state;

import escrims.domain.matchmaking.ByHistoryStrategy;
import escrims.domain.matchmaking.ByLatencyStrategy;
import escrims.domain.matchmaking.ByMMRStrategy;
import escrims.domain.matchmaking.CompositeMatchmakingStrategy;
import escrims.domain.matchmaking.MatchmakingStrategy;
import escrims.infra.events.DomainEventBus;
import escrims.infra.events.ScrimCreadoEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * PATRON BUILDER — Construcción incremental y validada de ScrimContext.
 *
 * Problema que resuelve:
 * ScrimContext tiene múltiples parámetros en su constructor. Construirlo directamente
 * es propenso a errores (parámetros en orden incorrecto, valores nulos o inválidos).
 * El Builder permite construir el objeto paso a paso con una interfaz fluida (fluent)
 * y garantiza que el ScrimContext resultante siempre sea válido.
 *
 * Además, configura una estrategia de matchmaking por defecto si el cliente no
 * especifica una explícitamente.
 *
 * SOLID SRP:
 * ScrimBuilder tiene una sola responsabilidad: construir y validar ScrimContext.
 *
 * SOLID OCP:
 * Se pueden agregar nuevas estrategias de matchmaking sin modificar ScrimContext.
 *
 * GRASP Creator:
 * ScrimBuilder es el creador natural de ScrimContext porque acumula todos los datos
 * necesarios para construirlo.
 *
 * GRASP High Cohesion:
 * toda la lógica de construcción y validación está aquí, no dispersa.
 */
public class ScrimBuilder {

    private String juego;
    private String formato;
    private String region;
    private int rangoMin = -1;
    private int rangoMax = -1;
    private int latenciaMax = -1;
    private LocalDateTime fechaHora;
    private int duracionMinutos = -1;
    private int cuposTotales = -1;

    private MatchmakingStrategy matchmakingStrategy;

    private final DomainEventBus eventBus;

    public ScrimBuilder(DomainEventBus eventBus) {
        if (eventBus == null) {
            throw new IllegalArgumentException("eventBus no puede ser null");
        }

        this.eventBus = eventBus;
    }

    public ScrimBuilder juego(String juego) {
        this.juego = juego;
        return this;
    }

    public ScrimBuilder formato(String formato) {
        this.formato = formato;
        return this;
    }

    public ScrimBuilder region(String region) {
        this.region = region;
        return this;
    }

    public ScrimBuilder rango(int rangoMin, int rangoMax) {
        this.rangoMin = rangoMin;
        this.rangoMax = rangoMax;
        return this;
    }

    public ScrimBuilder latenciaMax(int latenciaMax) {
        this.latenciaMax = latenciaMax;
        return this;
    }

    public ScrimBuilder fechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
        return this;
    }

    public ScrimBuilder duracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
        return this;
    }

    public ScrimBuilder cuposTotales(int cuposTotales) {
        this.cuposTotales = cuposTotales;
        return this;
    }

    public ScrimBuilder matchmakingStrategy(MatchmakingStrategy matchmakingStrategy) {
        this.matchmakingStrategy = matchmakingStrategy;
        return this;
    }

    public ScrimContext build() {
        validarInvariantes();

        UUID id = UUID.randomUUID();

        MatchmakingStrategy estrategiaFinal = obtenerEstrategiaDeMatchmaking();

        ScrimContext ctx = new ScrimContext(
                id,
                juego,
                formato,
                region,
                rangoMin,
                rangoMax,
                latenciaMax,
                fechaHora,
                duracionMinutos,
                cuposTotales,
                eventBus,
                estrategiaFinal
        );

        eventBus.publish(new ScrimCreadoEvent(id, juego, region, formato, cuposTotales));

        return ctx;
    }

    private MatchmakingStrategy obtenerEstrategiaDeMatchmaking() {
        if (matchmakingStrategy != null) {
            return matchmakingStrategy;
        }

        return new CompositeMatchmakingStrategy(
                List.of(
                        new ByMMRStrategy(),
                        new ByLatencyStrategy(),
                        new ByHistoryStrategy()
                )
        );
    }

    private void validarInvariantes() {
        if (juego == null || juego.isBlank()) {
            throw new IllegalStateException("El juego es obligatorio");
        }

        if (formato == null || formato.isBlank()) {
            throw new IllegalStateException("El formato es obligatorio (ej: 5v5, 2v2)");
        }

        if (region == null || region.isBlank()) {
            throw new IllegalStateException("La región es obligatoria");
        }

        if (rangoMin < 0) {
            throw new IllegalStateException("El rango mínimo es obligatorio y debe ser >= 0");
        }

        if (rangoMax < 0) {
            throw new IllegalStateException("El rango máximo es obligatorio y debe ser >= 0");
        }

        if (rangoMin >= rangoMax) {
            throw new IllegalStateException(
                    "rangoMin (" + rangoMin + ") debe ser menor que rangoMax (" + rangoMax + ")"
            );
        }

        if (latenciaMax <= 0) {
            throw new IllegalStateException("La latencia máxima debe ser mayor a 0 ms");
        }

        if (fechaHora == null) {
            throw new IllegalStateException("La fecha y hora del scrim es obligatoria");
        }

        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("La fecha y hora del scrim debe ser en el futuro");
        }

        if (duracionMinutos <= 0) {
            throw new IllegalStateException("La duración en minutos debe ser mayor a 0");
        }

        if (cuposTotales < 2) {
            throw new IllegalStateException("El scrim debe tener al menos 2 cupos");
        }

        if (cuposTotales % 2 != 0) {
            throw new IllegalStateException("Los cupos totales deben ser un número par (equipos iguales)");
        }
    }
}