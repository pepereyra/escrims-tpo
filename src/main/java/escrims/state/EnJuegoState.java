package escrims.state;

import escrims.dominio.Usuario;
import escrims.dominio.enums.Rol;
import escrims.observer.ScrimStateChangedEvent;

/**
 * PATRON STATE - Estado "En Juego".
 * El scrim está en curso. Solo se puede finalizar.
 */
public class EnJuegoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario u, Rol rol) {
        throw new IllegalStateException("No se puede postular: el scrim ya está en curso.");
    }

    @Override
    public void confirmar(ScrimContext ctx, Usuario u) {
        throw new IllegalStateException("No se puede confirmar: el scrim ya está en curso.");
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim ya está en curso.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        ctx.setState(new FinalizadoState());
        ctx.publicarEvento(new ScrimStateChangedEvent(ctx.getId(), "EN_JUEGO", "FINALIZADO"));
        System.out.println("[EnJuegoState] ¡El scrim ha finalizado! Se pueden cargar resultados.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede cancelar: el scrim ya está en curso.");
    }

    @Override
    public String getNombre() {
        return "EN_JUEGO";
    }
}
