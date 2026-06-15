package escrims.domain.command;

import escrims.domain.state.ScrimContext;

/**
 * PATRON COMMAND.
 * Encapsula una accion de gestion del scrim como objeto ejecutable.
 */
public interface ScrimCommand {

    void ejecutar();

    void deshacer();

    ScrimContext getScrim();
}
