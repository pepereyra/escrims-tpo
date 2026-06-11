package escrims.factory;

import escrims.dominio.Notificacion;
import escrims.dominio.Usuario;
import escrims.strategy.NotificadorStrategy;

/**
 * PATRON ABSTRACT FACTORY - Fábrica concreta para entorno de PRODUCCIÓN.
 *
 * Crea notificadores que representan integraciones reales con servicios externos:
 * - Email: simula JavaMail / SendGrid
 * - Push:  simula Firebase Cloud Messaging (FCM)
 * - Discord: simula Discord Webhooks
 *
 * En un sistema real, estas clases internas serían reemplazadas por adaptadores
 * que llaman a las APIs externas correspondientes (patrón Adapter).
 *
 * SOLID OCP: nueva familia sin modificar NotificadorFactory ni ScrimController.
 * SOLID DIP: ScrimController depende de NotificadorFactory, no de esta clase.
 * GRASP Low Coupling: el controller no conoce los detalles de producción.
 */
public class ProdNotificadorFactory implements NotificadorFactory {

    @Override
    public NotificadorStrategy crearEmailNotificador() {
        System.out.println("[ProdFactory] Creando EmailNotificador (modo PROD - JavaMail/SendGrid)");
        return new JavaMailNotificador();
    }

    @Override
    public NotificadorStrategy crearPushNotificador() {
        System.out.println("[ProdFactory] Creando PushNotificador (modo PROD - Firebase FCM)");
        return new FcmPushNotificador();
    }

    @Override
    public NotificadorStrategy crearDiscordNotificador() {
        System.out.println("[ProdFactory] Creando DiscordNotificador (modo PROD - Discord Webhook)");
        return new DiscordWebhookNotificador();
    }

    // -----------------------------------------------------------------------
    // Notificadores concretos de producción (clases internas estáticas)
    // En un proyecto real serían clases separadas con sus dependencias inyectadas.
    // -----------------------------------------------------------------------

    /**
     * Notificador de Email para producción.
     * Simula el envío real via JavaMail / SendGrid.
     */
    private static class JavaMailNotificador implements NotificadorStrategy {
        @Override
        public void enviar(Usuario destinatario, Notificacion notificacion) {
            notificacion.marcarEnviada();
            System.out.println("[PROD-EMAIL/JavaMail] → " + destinatario.getEmail() +
                    " | Evento: " + notificacion.getTipo() +
                    " | Mensaje: " + notificacion.getPayload());
            // En producción: new JavaMailSender().send(destinatario.getEmail(), ...)
        }
    }

    /**
     * Notificador Push para producción.
     * Simula el envío real via Firebase Cloud Messaging (FCM).
     */
    private static class FcmPushNotificador implements NotificadorStrategy {
        @Override
        public void enviar(Usuario destinatario, Notificacion notificacion) {
            notificacion.marcarEnviada();
            System.out.println("[PROD-PUSH/FCM] → " + destinatario.getUsername() +
                    " | Evento: " + notificacion.getTipo() +
                    " | Mensaje: " + notificacion.getPayload());
            // En producción: FirebaseMessaging.getInstance().send(Message.builder()...build())
        }
    }

    /**
     * Notificador Discord para producción.
     * Simula el envío real via Discord Webhook.
     */
    private static class DiscordWebhookNotificador implements NotificadorStrategy {
        @Override
        public void enviar(Usuario destinatario, Notificacion notificacion) {
            notificacion.marcarEnviada();
            System.out.println("[PROD-DISCORD/Webhook] → @" + destinatario.getUsername() +
                    " | Evento: " + notificacion.getTipo() +
                    " | Mensaje: " + notificacion.getPayload());
            // En producción: HttpClient.post(DISCORD_WEBHOOK_URL, payload)
        }
    }
}
