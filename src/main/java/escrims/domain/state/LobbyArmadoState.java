package escrims.domain.state;

import escrims.domain.model.Confirmacion;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.infra.events.ScrimStateChangedEvent;

/**
 * PATRON STATE - Estado "Lobby Armado".
 * Los cupos están completos. Los jugadores deben confirmar su asistencia.
 * Si todos confirman → ConfirmadoState. Si alguien no confirma → BuscandoState.
 */
public class LobbyArmadoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario u, Rol rol) {
        throw new IllegalStateException("No se puede postular: el lobby ya está completo.");
    }

    @Override
    public void confirmar(ScrimContext ctx, Usuario u) {
        Confirmacion conf = ctx.getConfirmacionDeUsuario(u);
        if (conf == null) {
            throw new IllegalArgumentException("El usuario " + u.getUsername() + " no está en este scrim.");
        }
        if (conf.isConfirmado()) {
            System.out.println("[LobbyArmadoState] " + u.getUsername() + " ya había confirmado.");
            return;
        }
        conf.confirmar();
        System.out.println("[LobbyArmadoState] " + u.getUsername() + " confirmó asistencia.");

        if (ctx.todosConfirmaron()) {
            ctx.setState(new ConfirmadoState());
            ctx.publicarEvento(new ScrimStateChangedEvent(ctx.getId(), "LOBBY_ARMADO", "CONFIRMADO"));
        }
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede iniciar: faltan confirmaciones de los jugadores.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede finalizar: el scrim no ha comenzado.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        ctx.setState(new CanceladoState());
        ctx.publicarEvento(new ScrimStateChangedEvent(ctx.getId(), "LOBBY_ARMADO", "CANCELADO"));
        System.out.println("[LobbyArmadoState] Scrim cancelado.");
    }

    @Override
    public String getNombre() {
        return "LOBBY_ARMADO";
    }
}
