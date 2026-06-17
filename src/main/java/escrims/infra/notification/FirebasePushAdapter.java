package escrims.infra.notification;

import escrims.domain.model.Notificacion;
import escrims.domain.model.Usuario;

/**
 * PATRON ADAPTER - adapta el contrato interno NotificadorStrategy a un
 * proveedor externo de push notifications tipo Firebase Cloud Messaging.
 */
public class FirebasePushAdapter implements NotificadorStrategy {

    @Override
    public String getCanal() {
        return "PUSH";
    }

    @Override
    public void enviar(Usuario destinatario, Notificacion notificacion) {
        notificacion.marcarEnviada();
        System.out.println("[ADAPTER-FIREBASE] token=simulado-" + destinatario.getId()
                + " | title=eScrims"
                + " | event=" + notificacion.getTipo()
                + " | body=" + notificacion.getPayload());
    }
}
