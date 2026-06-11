package escrims.strategy;

import escrims.dominio.Notificacion;
import escrims.dominio.Usuario;

/**
 * PATRON STRATEGY - Estrategia concreta: notificación por Push Notification.
 * SOLID OCP: nueva estrategia sin modificar NotificadorStrategy ni otras estrategias.
 */
public class PushNotificador implements NotificadorStrategy {

    @Override
    public void enviar(Usuario destinatario, Notificacion notificacion) {
        notificacion.marcarEnviada();
        System.out.println("[PUSH] → @" + destinatario.getUsername() +
                " | Evento: " + notificacion.getTipo() +
                " | Mensaje: " + notificacion.getPayload());
    }
}
