package escrims.domain.matchmaking;

import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;

/**
 * PATRON STRATEGY - Matchmaking por historial/comportamiento.
 *
 * Simula una validación por historial usando strikes y cooldown.
 * En una versión más completa podría considerar abandonos previos,
 * reportes de conducta, compatibilidad de roles o fair play.
 */
public class ByHistoryStrategy implements MatchmakingStrategy {

    @Override
    public boolean esCompatible(Usuario usuario, ScrimContext scrim) {
        return usuario.getStrikes() < 3
                && !usuario.isCooldownActivo();
    }
}