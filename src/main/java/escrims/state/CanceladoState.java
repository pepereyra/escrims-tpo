package escrims.state;

import escrims.dominio.Usuario;
import escrims.dominio.enums.Rol;

/**
 * PATRON STATE - Estado "Cancelado" (terminal).
 * El scrim fue cancelado. No se permiten más transiciones.
 */
public class CanceladoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario u, Rol rol) {
        throw new IllegalStateException("El scrim fue cancelado.");
    }

    @Override
    public void confirmar(ScrimContext ctx, Usuario u) {
        throw new IllegalStateException("El scrim fue cancelado.");
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim fue cancelado.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim fue cancelado.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        System.out.println("[CanceladoState] El scrim ya está cancelado.");
    }

    @Override
    public String getNombre() {
        return "CANCELADO";
    }
}
