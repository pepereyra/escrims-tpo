package escrims.infra.notification;

import escrims.domain.model.Notificacion;
import escrims.domain.model.Usuario;

/**
 * PATRON ADAPTER - adapta el contrato interno NotificadorStrategy a un
 * webhook de Discord/Slack.
 */
public class DiscordWebhookAdapter implements NotificadorStrategy {

    @Override
    public String getCanal() {
        return "DISCORD";
    }

    @Override
    public void enviar(Usuario destinatario, Notificacion notificacion) {
        notificacion.marcarEnviada();
        System.out.println("[ADAPTER-DISCORD] webhook=/scrims"
                + " | mention=@" + destinatario.getUsername()
                + " | event=" + notificacion.getTipo()
                + " | content=" + notificacion.getPayload());
    }
}
