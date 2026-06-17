package escrims.domain.command;

import escrims.domain.model.Confirmacion;
import escrims.domain.model.Postulacion;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;
import escrims.domain.state.ScrimState;

public class MoverASuplenteCommand implements ScrimCommand {

    private final ScrimContext scrim;
    private final Usuario usuario;
    private Rol rolAnterior;
    private String estadoPostulacionAnterior;
    private Confirmacion confirmacionAnterior;
    private ScrimState estadoScrimAnterior;
    private boolean ejecutado;

    public MoverASuplenteCommand(ScrimContext scrim, Usuario usuario) {
        this.scrim = scrim;
        this.usuario = usuario;
    }

    @Override
    public void ejecutar() {
        Postulacion postulacion = scrim.getPostulaciones().stream()
                .filter(Postulacion::estaAceptada)
                .filter(p -> p.getUsuario().getId().equals(usuario.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario " + usuario.getUsername() + " no tiene una postulacion aceptada en este scrim."
                ));
        rolAnterior = postulacion.getRolDeseado();
        estadoPostulacionAnterior = postulacion.getEstado().getNombre();
        confirmacionAnterior = scrim.getConfirmacionDeUsuario(usuario);
        estadoScrimAnterior = scrim.getState();
        scrim.moverASuplente(usuario);
        ejecutado = true;
    }

    @Override
    public void deshacer() {
        if (!ejecutado) {
            throw new IllegalStateException("No se puede deshacer un comando que no fue ejecutado.");
        }
        scrim.restaurarPostulacion(usuario, rolAnterior, estadoPostulacionAnterior);
        scrim.restaurarConfirmacion(confirmacionAnterior);
        scrim.restaurarEstado(estadoScrimAnterior);
    }

    @Override
    public ScrimContext getScrim() {
        return scrim;
    }
}
