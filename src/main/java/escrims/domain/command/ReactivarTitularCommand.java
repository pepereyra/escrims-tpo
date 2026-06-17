package escrims.domain.command;

import escrims.domain.model.Confirmacion;
import escrims.domain.model.Postulacion;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;
import escrims.domain.state.ScrimState;

public class ReactivarTitularCommand implements ScrimCommand {

    private final ScrimContext scrim;
    private final Usuario usuario;
    private Rol rolAnterior;
    private String estadoPostulacionAnterior;
    private Confirmacion confirmacionAnterior;
    private ScrimState estadoScrimAnterior;
    private boolean ejecutado;

    public ReactivarTitularCommand(ScrimContext scrim, Usuario usuario) {
        this.scrim = scrim;
        this.usuario = usuario;
    }

    @Override
    public void ejecutar() {
        Postulacion postulacion = scrim.getPostulaciones().stream()
                .filter(p -> p.getUsuario().getId().equals(usuario.getId()))
                .filter(p -> "SUPLENTE".equals(p.getEstado().getNombre()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario " + usuario.getUsername() + " no esta como suplente en este scrim."
                ));
        rolAnterior = postulacion.getRolDeseado();
        estadoPostulacionAnterior = postulacion.getEstado().getNombre();
        confirmacionAnterior = scrim.getConfirmacionDeUsuario(usuario);
        estadoScrimAnterior = scrim.getState();
        scrim.reactivarTitular(usuario);
        ejecutado = true;
    }

    @Override
    public void deshacer() {
        if (!ejecutado) {
            throw new IllegalStateException("No se puede deshacer un comando que no fue ejecutado.");
        }
        scrim.restaurarPostulacion(usuario, rolAnterior, estadoPostulacionAnterior);
        if (confirmacionAnterior == null) {
            scrim.removerConfirmacion(usuario);
        } else {
            scrim.restaurarConfirmacion(confirmacionAnterior);
        }
        scrim.restaurarEstado(estadoScrimAnterior);
    }

    @Override
    public ScrimContext getScrim() {
        return scrim;
    }
}
