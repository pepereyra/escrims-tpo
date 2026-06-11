package escrims.strategy;

import escrims.dominio.Notificacion;
import escrims.dominio.Usuario;

/**
 * PATRON STRATEGY - Estrategia concreta: notificación por Email.
 * SOLID OCP: nueva estrategia sin modificar NotificadorStrategy ni otras estrategias.
 */
public class EmailNotificador implements NotificadorStrategy {

    @Override
    public void enviar(Usuario destinatario, Notificacion notificacion) {
        notificacion.marcarEnviada();
        System.out.println("[EMAIL] → " + destinatario.getEmail() +
                " | Evento: " + notificacion.getTipo() +
                " | Mensaje: " + notificacion.getPayload());
    }
}
