package escrims.controller.api;

import escrims.controller.ScrimController;
import escrims.facade.ScrimFacade;
import escrims.infra.events.DomainEventBus;
import escrims.infra.notification.DevNotificadorFactory;
import escrims.infra.notification.NotificadorFactory;
import escrims.service.AlertaBusquedaRepository;
import escrims.service.AuthService;
import escrims.service.AuditLogRepository;
import escrims.service.AuditService;
import escrims.service.BusquedaFavoritaRepository;
import escrims.service.BusquedaFavoritaService;
import escrims.service.FeedbackRepository;
import escrims.service.JwtService;
import escrims.service.ModeracionService;
import escrims.service.PasswordHasher;
import escrims.service.ReporteConductaRepository;
import escrims.service.ScrimRepository;
import escrims.service.ScrimService;
import org.springframework.beans.factory.annotation.Value;
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
    public PasswordHasher passwordHasher() {
        return new PasswordHasher();
    }

    @Bean
    public JwtService jwtService(@Value("${app.jwt.secret:dev-secret-change-me-12345678901234567890}") String secret) {
        return new JwtService(secret);
    }

    @Bean
    public AuthService authService(UsuarioApiRepository usuarios,
                                   PasswordHasher passwordHasher,
                                   JwtService jwtService) {
        return new AuthService(usuarios, passwordHasher, jwtService);
    }

    @Bean
    public ScrimService scrimService(DomainEventBus eventBus,
                                     NotificadorFactory factory,
                                     ScrimRepository scrimRepository) {
        return new ScrimService(eventBus, factory, scrimRepository);
    }

    @Bean
    public ScrimController scrimController(ScrimService service) {
        return new ScrimController(service);
    }

    @Bean
    public ScrimFacade scrimFacade(ScrimController controller) {
        return new ScrimFacade(controller);
    }

    @Bean
    public BusquedaFavoritaService busquedaFavoritaService(DomainEventBus eventBus,
                                                           BusquedaFavoritaRepository busquedaRepository,
                                                           AlertaBusquedaRepository alertaRepository) {
        BusquedaFavoritaService service = new BusquedaFavoritaService(busquedaRepository, alertaRepository);
        eventBus.subscribe(service);
        return service;
    }

    @Bean
    public AuditService auditService(DomainEventBus eventBus,
                                     AuditLogRepository auditLogRepository) {
        AuditService service = new AuditService(auditLogRepository);
        eventBus.subscribe(service);
        return service;
    }

    @Bean
    public ModeracionService moderacionService(ScrimService scrimService,
                                               FeedbackRepository feedbackRepository,
                                               ReporteConductaRepository reporteRepository) {
        return new ModeracionService(scrimService, feedbackRepository, reporteRepository);
    }
}
