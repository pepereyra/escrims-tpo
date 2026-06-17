package escrims.domain.command;

import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;

public class CambiarRolCommand implements ScrimCommand {

    private final ScrimContext scrim;
    private final Usuario usuario;
    private final Rol nuevoRol;
    private Rol rolAnterior;
    private boolean ejecutado;

    public CambiarRolCommand(ScrimContext scrim, Usuario usuario, Rol nuevoRol) {
        this.scrim = scrim;
        this.usuario = usuario;
        this.nuevoRol = nuevoRol;
    }

    @Override
    public void ejecutar() {
        rolAnterior = scrim.getPostulaciones().stream()
                .filter(postulacion -> postulacion.estaAceptada())
                .filter(postulacion -> postulacion.getUsuario().getId().equals(usuario.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario " + usuario.getUsername() + " no tiene una postulacion aceptada en este scrim."
                ))
                .getRolDeseado();
        scrim.cambiarRol(usuario, nuevoRol);
        ejecutado = true;
    }

    @Override
    public void deshacer() {
        if (!ejecutado) {
            throw new IllegalStateException("No se puede deshacer un comando que no fue ejecutado.");
        }
        scrim.cambiarRol(usuario, rolAnterior);
    }

    @Override
    public ScrimContext getScrim() {
        return scrim;
    }
}
