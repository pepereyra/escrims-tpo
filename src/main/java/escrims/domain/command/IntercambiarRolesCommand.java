package escrims.domain.command;

import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;

public class IntercambiarRolesCommand implements ScrimCommand {

    private final ScrimContext scrim;
    private final Usuario usuarioA;
    private final Usuario usuarioB;
    private Rol rolAnteriorA;
    private Rol rolAnteriorB;
    private boolean ejecutado;

    public IntercambiarRolesCommand(ScrimContext scrim, Usuario usuarioA, Usuario usuarioB) {
        this.scrim = scrim;
        this.usuarioA = usuarioA;
        this.usuarioB = usuarioB;
    }

    @Override
    public void ejecutar() {
        rolAnteriorA = rolAceptadoDe(usuarioA);
        rolAnteriorB = rolAceptadoDe(usuarioB);
        scrim.intercambiarRoles(usuarioA, usuarioB);
        ejecutado = true;
    }

    @Override
    public void deshacer() {
        if (!ejecutado) {
            throw new IllegalStateException("No se puede deshacer un comando que no fue ejecutado.");
        }
        scrim.cambiarRol(usuarioA, rolAnteriorA);
        scrim.cambiarRol(usuarioB, rolAnteriorB);
    }

    private Rol rolAceptadoDe(Usuario usuario) {
        return scrim.getPostulaciones().stream()
                .filter(postulacion -> postulacion.estaAceptada())
                .filter(postulacion -> postulacion.getUsuario().getId().equals(usuario.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario " + usuario.getUsername() + " no tiene una postulacion aceptada en este scrim."
                ))
                .getRolDeseado();
    }

    @Override
    public ScrimContext getScrim() {
        return scrim;
    }
}
