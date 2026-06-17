package escrims.infra.notification;

/**
 * PATRON ABSTRACT FACTORY - Interfaz de la fábrica de notificadores.
 *
 * Define el contrato para crear familias de objetos NotificadorStrategy
 * sin acoplar el código cliente a las implementaciones concretas.
 *
 * SOLID OCP: se pueden agregar nuevas familias (ej: SmsNotificadorFactory)
 *            sin modificar el código existente.
 * SOLID DIP: ScrimController depende de esta abstracción, no de las
 *            fábricas concretas ni de los notificadores concretos.
 * GRASP Low Coupling: el controller no sabe qué notificador concreto se usa.
 */
public interface NotificadorFactory {

    /**
     * Crea el notificador para el canal Email.
     */
    NotificadorStrategy crearEmailNotificador();

    /**
     * Crea el notificador para el canal Push Notification.
     */
    NotificadorStrategy crearPushNotificador();

    /**
     * Crea el notificador para el canal Discord.
     */
    NotificadorStrategy crearDiscordNotificador();
}
