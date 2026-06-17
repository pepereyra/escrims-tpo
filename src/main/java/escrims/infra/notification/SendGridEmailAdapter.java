package escrims.infra.notification;

import escrims.domain.model.Notificacion;
import escrims.domain.model.Usuario;

/**
 * PATRON ADAPTER - adapta el contrato interno NotificadorStrategy a un
 * proveedor externo de email tipo SendGrid.
 *
 * En esta entrega se simula la llamada real para mantener el proyecto
 * ejecutable sin credenciales ni servicios externos.
 */
public class SendGridEmailAdapter implements NotificadorStrategy {

    @Override
    public String getCanal() {
        return "EMAIL";
    }

    @Override
    public void enviar(Usuario destinatario, Notificacion notificacion) {
        notificacion.marcarEnviada();
        System.out.println("[ADAPTER-SENDGRID] email=" + destinatario.getEmail()
                + " | subject=eScrims: " + notificacion.getTipo()
                + " | body=" + notificacion.getPayload());
    }
}
