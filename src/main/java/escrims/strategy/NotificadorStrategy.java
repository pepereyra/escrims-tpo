package escrims.strategy;

import escrims.dominio.Notificacion;
import escrims.dominio.Usuario;

/**
 * PATRON STRATEGY - Interfaz de la estrategia de notificación.
 * Define el contrato para enviar notificaciones.
 * SOLID OCP: se pueden agregar nuevos canales sin modificar el código existente.
 * SOLID DIP: el NotificationSubscriber depende de esta abstracción, no de implementaciones concretas.
 */
public interface NotificadorStrategy {
    void enviar(Usuario destinatario, Notificacion notificacion);
}
