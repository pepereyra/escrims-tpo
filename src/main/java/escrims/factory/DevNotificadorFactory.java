package escrims.factory;

import escrims.strategy.DiscordNotificador;
import escrims.strategy.EmailNotificador;
import escrims.strategy.NotificadorStrategy;
import escrims.strategy.PushNotificador;

/**
 * PATRON ABSTRACT FACTORY - Fábrica concreta para entorno de DESARROLLO.
 *
 * Crea notificadores que simulan el envío real: solo imprimen en consola.
 * Útil para testing y desarrollo local sin depender de servicios externos.
 *
 * SOLID OCP: nueva familia de notificadores sin modificar NotificadorFactory
 *            ni las clases cliente (ScrimController).
 * GRASP Creator: crea los objetos NotificadorStrategy que necesita el sistema.
 */
public class DevNotificadorFactory implements NotificadorFactory {

    @Override
    public NotificadorStrategy crearEmailNotificador() {
        System.out.println("[DevFactory] Creando EmailNotificador (modo DEV - simula envío)");
        return new EmailNotificador();
    }

    @Override
    public NotificadorStrategy crearPushNotificador() {
        System.out.println("[DevFactory] Creando PushNotificador (modo DEV - simula envío)");
        return new PushNotificador();
    }

    @Override
    public NotificadorStrategy crearDiscordNotificador() {
        System.out.println("[DevFactory] Creando DiscordNotificador (modo DEV - simula envío)");
        return new DiscordNotificador();
    }
}
