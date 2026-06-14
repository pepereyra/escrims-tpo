package escrims.domain.command;

import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;

public class CambiarRolCommand implements ScrimCommand {

    private final ScrimContext scrim;
    private final Usuario usuario;
    private final Rol nuevoRol;

    public CambiarRolCommand(ScrimContext scrim, Usuario usuario, Rol nuevoRol) {
        this.scrim = scrim;
        this.usuario = usuario;
        this.nuevoRol = nuevoRol;
    }

    @Override
    public void ejecutar() {
        scrim.cambiarRol(usuario, nuevoRol);
    }
}
