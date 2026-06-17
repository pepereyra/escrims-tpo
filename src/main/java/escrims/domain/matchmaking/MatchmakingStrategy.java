package escrims.domain.matchmaking;

import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;

/**
 * PATRON STRATEGY
 *
 * Define el algoritmo de emparejamiento utilizado por un Scrim.
 *
 * La estrategia determina si un usuario es compatible con el scrim
 * según distintos criterios (MMR, latencia, historial, etc.).
 *
 * SOLID OCP:
 * nuevas estrategias pueden agregarse sin modificar el código existente.
 *
 * SOLID DIP:
 * ScrimContext depende de esta abstracción y no de implementaciones concretas.
 */
public interface MatchmakingStrategy {

    boolean esCompatible(Usuario usuario, ScrimContext scrim);

}