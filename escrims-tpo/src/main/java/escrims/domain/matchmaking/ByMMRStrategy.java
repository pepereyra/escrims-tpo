package escrims.domain.matchmaking;

import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;

/**
 * PATRON STRATEGY - Matchmaking por rango/MMR.
 *
 * Acepta únicamente jugadores cuyo rango esté dentro
 * de los límites definidos por el scrim.
 */
public class ByMMRStrategy implements MatchmakingStrategy {

    @Override
    public boolean esCompatible(Usuario usuario, ScrimContext scrim) {

        int rangoUsuario =
                usuario.getRangoEnJuego(scrim.getJuego());

        return rangoUsuario >= scrim.getRangoMin()
                && rangoUsuario <= scrim.getRangoMax();
    }
}