package escrims.controller.api;

import escrims.controller.ScrimController;
import escrims.facade.ScrimFacade;
import escrims.infra.events.DomainEventBus;
import escrims.infra.notification.DevNotificadorFactory;
import escrims.infra.notification.NotificadorFactory;
import escrims.service.ScrimService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

    @Bean
    public DomainEventBus domainEventBus() {
        return new DomainEventBus();
    }

    @Bean
    public NotificadorFactory notificadorFactory() {
        return new DevNotificadorFactory();
    }

    @Bean
    public ScrimService scrimService(DomainEventBus eventBus, NotificadorFactory factory) {
        return new ScrimService(eventBus, factory);
    }

    @Bean
    public ScrimController scrimController(ScrimService service) {
        return new ScrimController(service);
    }

    @Bean
    public ScrimFacade scrimFacade(ScrimController controller) {
        return new ScrimFacade(controller);
    }
}
