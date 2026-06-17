package escrims.domain.state;

import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;

/**
 * PATRON STATE - Estado "Finalizado" (terminal).
 * El scrim terminó. No se permiten más transiciones.
 */
public class FinalizadoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario u, Rol rol) {
        throw new IllegalStateException("El scrim ya finalizó.");
    }

    @Override
    public void confirmar(ScrimContext ctx, Usuario u) {
        throw new IllegalStateException("El scrim ya finalizó.");
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim ya finalizó.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim ya finalizó.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim ya finalizó, no se puede cancelar.");
    }

    @Override
    public String getNombre() {
        return "FINALIZADO";
    }
}
