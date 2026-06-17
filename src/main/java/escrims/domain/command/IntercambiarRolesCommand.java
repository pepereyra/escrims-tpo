package escrims.domain.command;

import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;

public class IntercambiarRolesCommand implements ScrimCommand {

    private final ScrimContext scrim;
    private final Usuario usuarioA;
    private final Usuario usuarioB;

    public IntercambiarRolesCommand(ScrimContext scrim, Usuario usuarioA, Usuario usuarioB) {
        this.scrim = scrim;
        this.usuarioA = usuarioA;
        this.usuarioB = usuarioB;
    }

    @Override
    public void ejecutar() {
        scrim.intercambiarRoles(usuarioA, usuarioB);
    }
}
