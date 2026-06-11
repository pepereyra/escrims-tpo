package escrims.state;

import escrims.dominio.Confirmacion;
import escrims.dominio.Postulacion;
import escrims.dominio.Usuario;
import escrims.dominio.enums.Rol;
import escrims.observer.ScrimStateChangedEvent;

/**
 * PATRON STATE - Estado "Buscando Jugadores".
 * Permite postulaciones. Cuando se completan los cupos, transiciona a LobbyArmadoState.
 * GRASP Creator: crea Postulacion y Confirmacion (las contiene y registra).
 */
public class BuscandoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario u, Rol rol) {
        // Validaciones de negocio
        if (!u.isVerificado()) {
            throw new IllegalArgumentException("El usuario " + u.getUsername() + " no tiene el email verificado.");
        }
        if (u.isCooldownActivo()) {
            throw new IllegalStateException("El usuario " + u.getUsername() + " tiene un cooldown activo.");
        }
        if (ctx.usuarioYaPostulado(u)) {
            throw new IllegalStateException("El usuario " + u.getUsername() + " ya está postulado a este scrim.");
        }
        int rangoUsuario = u.getRangoEnJuego(ctx.getJuego());
        if (rangoUsuario < ctx.getRangoMin() || rangoUsuario > ctx.getRangoMax()) {
            throw new IllegalArgumentException("El rango del usuario (" + rangoUsuario +
                    ") no está dentro del rango permitido [" + ctx.getRangoMin() + ", " + ctx.getRangoMax() + "].");
        }
        if (u.getLatenciaPromedio() > ctx.getLatenciaMax()) {
            throw new IllegalArgumentException("La latencia del usuario (" + u.getLatenciaPromedio() +
                    "ms) supera el máximo permitido (" + ctx.getLatenciaMax() + "ms).");
        }
        if (ctx.cuposDisponibles() <= 0) {
            throw new IllegalStateException("No hay cupos disponibles en este scrim.");
        }

        // GRASP Creator: BuscandoState crea la Postulacion y la Confirmacion
        Postulacion postulacion = new Postulacion(u, rol);
        postulacion.aceptar();
        ctx.getPostulaciones().add(postulacion);

        Confirmacion confirmacion = new Confirmacion(u);
        ctx.getConfirmaciones().add(confirmacion);

        System.out.println("[BuscandoState] " + u.getUsername() + " se postuló como " + rol +
                ". Cupos restantes: " + ctx.cuposDisponibles());

        // Transición automática si se completaron los cupos
        if (ctx.cuposDisponibles() == 0) {
            ctx.setState(new LobbyArmadoState());
            ctx.publicarEvento(new ScrimStateChangedEvent(ctx.getId(), "BUSCANDO", "LOBBY_ARMADO"));
        }
    }

    @Override
    public void confirmar(ScrimContext ctx, Usuario u) {
        throw new IllegalStateException("No se puede confirmar: el scrim aún está buscando jugadores.");
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede iniciar: el scrim aún está buscando jugadores.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede finalizar: el scrim aún está buscando jugadores.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        ctx.setState(new CanceladoState());
        ctx.publicarEvento(new ScrimStateChangedEvent(ctx.getId(), "BUSCANDO", "CANCELADO"));
        System.out.println("[BuscandoState] Scrim cancelado por el organizador.");
    }

    @Override
    public String getNombre() {
        return "BUSCANDO";
    }
}
