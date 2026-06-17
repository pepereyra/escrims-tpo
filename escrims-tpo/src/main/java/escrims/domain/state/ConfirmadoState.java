package escrims.domain.state;

import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.infra.events.ScrimStateChangedEvent;

/**
 * PATRON STATE - Estado "Confirmado".
 * Todos los jugadores confirmaron. El organizador puede iniciar el scrim.
 */
public class ConfirmadoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario u, Rol rol) {
        throw new IllegalStateException("No se puede postular: el scrim ya está confirmado.");
    }

    @Override
    public void confirmar(ScrimContext ctx, Usuario u) {
        System.out.println("[ConfirmadoState] Todos ya confirmaron. No hay acción pendiente.");
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        ctx.setState(new EnJuegoState());
        ctx.publicarEvento(new ScrimStateChangedEvent(ctx.getId(), "CONFIRMADO", "EN_JUEGO"));
        System.out.println("[ConfirmadoState] ¡El scrim ha comenzado!");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede finalizar: el scrim aún no ha comenzado.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        ctx.setState(new CanceladoState());
        ctx.publicarEvento(new ScrimStateChangedEvent(ctx.getId(), "CONFIRMADO", "CANCELADO"));
        System.out.println("[ConfirmadoState] Scrim cancelado antes de iniciar.");
    }

    @Override
    public String getNombre() {
        return "CONFIRMADO";
    }
}
