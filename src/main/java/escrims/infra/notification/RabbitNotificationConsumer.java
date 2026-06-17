package escrims.infra.notification;

import escrims.domain.model.Notificacion;
import escrims.domain.model.Usuario;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class RabbitNotificationConsumer {

    private final NotificadorFactory notificadorFactory;

    public RabbitNotificationConsumer(NotificadorFactory notificadorFactory) {
        this.notificadorFactory = notificadorFactory;
    }

    @RabbitListener(queues = NotificationQueueNames.QUEUE)
    public void consumir(NotificationQueueMessage message) {
        NotificadorStrategy notificador = resolverNotificador(message.canal());
        Usuario usuario = new Usuario(message.usuarioId(), message.username(), message.email(), "", "SA", true);
        Notificacion notificacion = new Notificacion(message.tipo(), message.canal(), message.payload());

        notificador.enviar(usuario, notificacion);
    }

    private NotificadorStrategy resolverNotificador(String canal) {
        if ("PUSH".equalsIgnoreCase(canal)) {
            return notificadorFactory.crearPushNotificador();
        }
        if ("DISCORD".equalsIgnoreCase(canal)) {
            return notificadorFactory.crearDiscordNotificador();
        }
        return notificadorFactory.crearEmailNotificador();
    }
}
