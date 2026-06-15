package escrims.domain.state;

import escrims.domain.model.Confirmacion;
import escrims.domain.model.Postulacion;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.rules.GameRulesRegistry;
import escrims.infra.events.ScrimStateChangedEvent;

/**
 * PATRON STATE - Estado "Buscando Jugadores".
 * Permite postulaciones. Cuando se completan los cupos, transiciona a LobbyArmadoState.
 *
 * PATRON STRATEGY:
 * delega las reglas de emparejamiento en MatchmakingStrategy.
 * De esta forma, este estado no conoce si el criterio es MMR, latencia,
 * historial o una combinación de estrategias.
 *
 * GRASP Creator:
 * crea Postulacion y Confirmacion porque las contiene y registra en el contexto.
 *
 * GRASP Low Coupling:
 * no depende de estrategias concretas de matchmaking.
 */
public class BuscandoState implements ScrimState {

    private final GameRulesRegistry gameRulesRegistry = new GameRulesRegistry();

    @Override
    public void postular(ScrimContext ctx, Usuario u, Rol rol) {
        if (!u.isVerificado()) {
            throw new IllegalArgumentException(
                    "El usuario " + u.getUsername() + " no tiene el email verificado."
            );
        }

        if (ctx.usuarioYaPostulado(u)) {
            throw new IllegalStateException(
                    "El usuario " + u.getUsername() + " ya está postulado a este scrim."
            );
        }

        if (ctx.cuposDisponibles() <= 0) {
            throw new IllegalStateException(
                    "No hay cupos disponibles en este scrim."
            );
        }

        if (!ctx.getMatchmakingStrategy().esCompatible(u, ctx)) {
            throw new IllegalArgumentException(
                    "El usuario " + u.getUsername()
                            + " no cumple con las reglas de matchmaking configuradas."
            );
        }

        gameRulesRegistry.obtenerPara(ctx.getJuego()).validarPostulacion(ctx, rol);

        Postulacion postulacion = new Postulacion(u, rol);
        postulacion.aceptar();
        ctx.getPostulaciones().add(postulacion);

        Confirmacion confirmacion = new Confirmacion(u);
        ctx.getConfirmaciones().add(confirmacion);

        System.out.println("[BuscandoState] " + u.getUsername()
                + " se postuló como " + rol
                + ". Cupos restantes: " + ctx.cuposDisponibles());

        if (ctx.cuposDisponibles() == 0) {
            ctx.setState(new LobbyArmadoState());
            ctx.publicarEvento(
                    new ScrimStateChangedEvent(
                            ctx.getId(),
                            "BUSCANDO",
                            "LOBBY_ARMADO"
                    )
            );
        }
    }

    @Override
    public void confirmar(ScrimContext ctx, Usuario u) {
        throw new IllegalStateException(
                "No se puede confirmar: el scrim aún está buscando jugadores."
        );
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException(
                "No se puede iniciar: el scrim aún está buscando jugadores."
        );
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException(
                "No se puede finalizar: el scrim aún está buscando jugadores."
        );
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        ctx.setState(new CanceladoState());
        ctx.publicarEvento(
                new ScrimStateChangedEvent(
                        ctx.getId(),
                        "BUSCANDO",
                        "CANCELADO"
                )
        );

        System.out.println("[BuscandoState] Scrim cancelado por el organizador.");
    }

    @Override
    public String getNombre() {
        return "BUSCANDO";
    }
}
