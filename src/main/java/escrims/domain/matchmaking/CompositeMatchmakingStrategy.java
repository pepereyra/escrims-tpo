package escrims.domain.matchmaking;

import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;

import java.util.List;

/**
 * PATRON STRATEGY - Estrategia compuesta de matchmaking.
 *
 * Permite combinar varias estrategias simples.
 * El usuario es compatible solo si cumple todas las estrategias configuradas.
 *
 * Esto permite aplicar simultáneamente:
 * - rango/MMR
 * - latencia
 * - historial/comportamiento
 */
public class CompositeMatchmakingStrategy implements MatchmakingStrategy {

    private final List<MatchmakingStrategy> strategies;

    public CompositeMatchmakingStrategy(List<MatchmakingStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("Debe existir al menos una estrategia de matchmaking.");
        }

        this.strategies = strategies;
    }

    @Override
    public boolean esCompatible(Usuario usuario, ScrimContext scrim) {
        return strategies.stream()
                .allMatch(strategy -> strategy.esCompatible(usuario, scrim));
    }
}