package escrims.service;

import escrims.domain.model.RecordatorioScrim;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ScrimReminderScheduler {

    private final ScrimService scrimService;
    private final boolean enabled;
    private final int horasAntes;

    public ScrimReminderScheduler(ScrimService scrimService,
                                  @Value("${app.reminders.scheduler.enabled:true}") boolean enabled,
                                  @Value("${app.reminders.horas-antes:24}") int horasAntes) {
        if (horasAntes <= 0) {
            throw new IllegalArgumentException("app.reminders.horas-antes debe ser mayor a cero.");
        }
        this.scrimService = scrimService;
        this.enabled = enabled;
        this.horasAntes = horasAntes;
    }

    @Scheduled(
            fixedDelayString = "${app.reminders.scheduler.fixed-delay-millis:60000}",
            initialDelayString = "${app.reminders.scheduler.initial-delay-millis:60000}"
    )
    public void procesarAutomaticamente() {
        if (!enabled) {
            return;
        }

        List<RecordatorioScrim> enviados = scrimService.procesarRecordatorios(LocalDateTime.now(), horasAntes);
        if (!enviados.isEmpty()) {
            System.out.println("[ScrimReminderScheduler] Recordatorios enviados: " + enviados.size());
        }
    }
}
