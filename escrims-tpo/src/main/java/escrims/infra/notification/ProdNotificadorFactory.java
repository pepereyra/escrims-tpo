package escrims.infra.notification;

/**
 * PATRON ABSTRACT FACTORY - Fábrica concreta para entorno de PRODUCCIÓN.
 *
 * Crea adapters que representan integraciones reales con servicios externos:
 * - Email: SendGrid
 * - Push: Firebase Cloud Messaging (FCM)
 * - Discord/Slack: Webhook
 *
 * SOLID OCP: nueva familia sin modificar NotificadorFactory ni ScrimController.
 * SOLID DIP: ScrimController depende de NotificadorFactory, no de esta clase.
 * GRASP Low Coupling: el controller no conoce los detalles de producción.
 */
public class ProdNotificadorFactory implements NotificadorFactory {

    @Override
    public NotificadorStrategy crearEmailNotificador() {
        System.out.println("[ProdFactory] Creando SendGridEmailAdapter (modo PROD)");
        return new SendGridEmailAdapter();
    }

    @Override
    public NotificadorStrategy crearPushNotificador() {
        System.out.println("[ProdFactory] Creando FirebasePushAdapter (modo PROD)");
        return new FirebasePushAdapter();
    }

    @Override
    public NotificadorStrategy crearDiscordNotificador() {
        System.out.println("[ProdFactory] Creando DiscordWebhookAdapter (modo PROD)");
        return new DiscordWebhookAdapter();
    }
}
