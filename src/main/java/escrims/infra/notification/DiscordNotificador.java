package escrims.infra.notification;

import escrims.domain.model.Notificacion;
import escrims.domain.model.Usuario;

/**
 * PATRON STRATEGY - Estrategia concreta: notificación por Discord.
 * SOLID OCP: nueva estrategia agregada sin modificar código existente.
 */
public class DiscordNotificador implements NotificadorStrategy {

    @Override
    public String getCanal() {
        return "DISCORD";
    }

    @Override
    public void enviar(Usuario destinatario, Notificacion notificacion) {
        notificacion.marcarEnviada();
        System.out.println("[DISCORD] → @" + destinatario.getUsername() +
                " | Evento: " + notificacion.getTipo() +
                " | Mensaje: " + notificacion.getPayload());
    }
}
