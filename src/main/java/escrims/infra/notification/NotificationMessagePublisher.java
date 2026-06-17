package escrims.infra.notification;

public interface NotificationMessagePublisher {

    void publish(NotificationQueueMessage message);
}
