package escrims.domain.matchmaking;

import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;

/**
 * PATRON STRATEGY - Matchmaking por latencia.
 *
 * Acepta únicamente jugadores cuya latencia promedio
 * no supere la latencia máxima definida por el scrim.
 */
public class ByLatencyStrategy implements MatchmakingStrategy {

    @Override
    public boolean esCompatible(Usuario usuario, ScrimContext scrim) {
        return usuario.getLatenciaPromedio() <= scrim.getLatenciaMax();
    }
}