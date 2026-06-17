package escrims.infra.notification;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitNotificationMessagePublisher implements NotificationMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitNotificationMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(NotificationQueueMessage message) {
        rabbitTemplate.convertAndSend(
                NotificationQueueNames.EXCHANGE,
                NotificationQueueNames.ROUTING_KEY,
                message
        );
    }
}
