package escrims;

import escrims.domain.model.Notificacion;
import escrims.domain.model.RecordatorioScrim;
import escrims.domain.model.Usuario;
import escrims.infra.events.DomainEventBus;
import escrims.infra.notification.DevNotificadorFactory;
import escrims.infra.notification.NotificationMessagePublisher;
import escrims.infra.notification.NotificationQueueMessage;
import escrims.infra.notification.NotificadorStrategy;
import escrims.infra.notification.QueuedNotificationDispatcher;
import escrims.infra.notification.RabbitNotificationDispatcher;
import escrims.service.FixedWindowRateLimiter;
import escrims.service.ScrimReminderScheduler;
import escrims.service.ScrimService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityPolicyTest {

    @Test
    @DisplayName("Rate limiter bloquea cuando se supera el limite de la ventana")
    void rateLimiterBloqueaCuandoSuperaLimite() {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(2, 60_000);

        assertTrue(limiter.allow("127.0.0.1"));
        assertTrue(limiter.allow("127.0.0.1"));
        assertFalse(limiter.allow("127.0.0.1"));
    }

    @Test
    @DisplayName("Cola de notificaciones reintenta y marca fallida al agotar intentos")
    void colaNotificacionesReintentaYMarcaFallida() {
        QueuedNotificationDispatcher dispatcher = new QueuedNotificationDispatcher();
        Usuario usuario = new Usuario("Notify", "notify@mail.com", "hash", "SA");
        Notificacion notificacion = new Notificacion("TEST", "FAIL", "payload");
        AtomicInteger intentos = new AtomicInteger();

        dispatcher.enqueue(usuario, notificacion, new NotificadorStrategy() {
            @Override
            public String getCanal() {
                return "FAIL";
            }

            @Override
            public void enviar(Usuario destinatario, Notificacion notificacion) {
                intentos.incrementAndGet();
                throw new IllegalStateException("Proveedor caido");
            }
        });

        assertEquals(3, intentos.get());
        assertEquals("FALLIDA", notificacion.getEstado().getNombre());
    }

    @Test
    @DisplayName("Cola externa publica mensajes de notificacion en RabbitMQ")
    void colaExternaPublicaMensajesEnRabbit() {
        CapturingPublisher publisher = new CapturingPublisher();
        RabbitNotificationDispatcher dispatcher = new RabbitNotificationDispatcher(publisher);
        Usuario usuario = new Usuario("RabbitUser", "rabbit@mail.com", "hash", "SA");
        Notificacion notificacion = new Notificacion("CONFIRMADO", "EMAIL", "payload");

        dispatcher.enqueue(usuario, notificacion, new DevNotificadorFactory().crearEmailNotificador());

        assertEquals("RabbitUser", publisher.lastMessage.username());
        assertEquals("EMAIL", publisher.lastMessage.canal());
        assertEquals("CONFIRMADO", publisher.lastMessage.tipo());
    }

    @Test
    @DisplayName("Scheduler de recordatorios ejecuta el procesamiento automatico si esta habilitado")
    void schedulerRecordatoriosEjecutaSiEstaHabilitado() {
        CountingScrimService scrimService = new CountingScrimService();
        ScrimReminderScheduler scheduler = new ScrimReminderScheduler(scrimService, true, 6);

        scheduler.procesarAutomaticamente();

        assertEquals(1, scrimService.ejecuciones.get());
        assertEquals(6, scrimService.ultimasHorasAntes);
    }

    @Test
    @DisplayName("Scheduler de recordatorios no ejecuta el procesamiento si esta deshabilitado")
    void schedulerRecordatoriosNoEjecutaSiEstaDeshabilitado() {
        CountingScrimService scrimService = new CountingScrimService();
        ScrimReminderScheduler scheduler = new ScrimReminderScheduler(scrimService, false, 6);

        scheduler.procesarAutomaticamente();

        assertEquals(0, scrimService.ejecuciones.get());
    }

    private static class CountingScrimService extends ScrimService {
        private final AtomicInteger ejecuciones = new AtomicInteger();
        private int ultimasHorasAntes;

        CountingScrimService() {
            super(new DomainEventBus(), new DevNotificadorFactory());
        }

        @Override
        public List<RecordatorioScrim> procesarRecordatorios(LocalDateTime ahora, int horasAntes) {
            ejecuciones.incrementAndGet();
            ultimasHorasAntes = horasAntes;
            return List.of();
        }
    }

    private static class CapturingPublisher implements NotificationMessagePublisher {
        private NotificationQueueMessage lastMessage;

        @Override
        public void publish(NotificationQueueMessage message) {
            this.lastMessage = message;
        }
    }
}
