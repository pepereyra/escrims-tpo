package escrims.infra.notification;

import escrims.domain.model.Notificacion;
import escrims.domain.model.Usuario;

public class RabbitNotificationDispatcher implements NotificationDispatcher {

    private final NotificationMessagePublisher publisher;

    public RabbitNotificationDispatcher(NotificationMessagePublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void enqueue(Usuario destinatario, Notificacion notificacion, NotificadorStrategy notificador) {
        try {
            publisher.publish(NotificationQueueMessage.from(destinatario, notificacion));
            System.out.println("[RabbitNotificationQueue] publicado canal=" + notificacion.getCanal()
                    + " destinatario=" + destinatario.getUsername());
        } catch (RuntimeException e) {
            notificacion.marcarFallida();
            throw e;
        }
    }

    @Override
    public int pendientes() {
        return 0;
    }
}
