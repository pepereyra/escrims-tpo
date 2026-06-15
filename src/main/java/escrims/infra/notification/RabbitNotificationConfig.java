package escrims.infra.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@ConditionalOnProperty(name = "app.notifications.queue", havingValue = "rabbit")
public class RabbitNotificationConfig {

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NotificationQueueNames.EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NotificationQueueNames.QUEUE, true);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange)
                .with(NotificationQueueNames.ROUTING_KEY);
    }

    @Bean
    public MessageConverter notificationMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter notificationMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(notificationMessageConverter);
        return template;
    }

    @Bean
    public NotificationMessagePublisher rabbitNotificationMessagePublisher(RabbitTemplate rabbitTemplate) {
        return new RabbitNotificationMessagePublisher(rabbitTemplate);
    }

    @Bean
    public NotificationDispatcher rabbitNotificationDispatcher(NotificationMessagePublisher publisher) {
        return new RabbitNotificationDispatcher(publisher);
    }

    @Bean
    public RabbitNotificationConsumer rabbitNotificationConsumer(NotificadorFactory notificadorFactory) {
        return new RabbitNotificationConsumer(notificadorFactory);
    }
}
