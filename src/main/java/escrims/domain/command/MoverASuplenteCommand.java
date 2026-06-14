package escrims.domain.command;

import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;

public class MoverASuplenteCommand implements ScrimCommand {

    private final ScrimContext scrim;
    private final Usuario usuario;

    public MoverASuplenteCommand(ScrimContext scrim, Usuario usuario) {
        this.scrim = scrim;
        this.usuario = usuario;
    }

    @Override
    public void ejecutar() {
        scrim.moverASuplente(usuario);
    }
}
