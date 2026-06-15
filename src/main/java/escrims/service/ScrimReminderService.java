package escrims.service;

import escrims.domain.model.Notificacion;
import escrims.domain.model.Postulacion;
import escrims.domain.model.RecordatorioScrim;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;
import escrims.infra.calendar.CalendarAdapter;
import escrims.infra.notification.NotificationDispatcher;
import escrims.infra.notification.NotificadorFactory;
import escrims.infra.notification.NotificadorStrategy;
import escrims.infra.notification.QueuedNotificationDispatcher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScrimReminderService {

    private final ScrimService scrimService;
    private final CalendarAdapter calendarAdapter;
    private final NotificadorFactory notificadorFactory;
    private final NotificationDispatcher dispatcher;
    private final Set<String> enviados = new HashSet<>();

    public ScrimReminderService(ScrimService scrimService,
                                CalendarAdapter calendarAdapter,
                                NotificadorFactory notificadorFactory) {
        this(scrimService, calendarAdapter, notificadorFactory, new QueuedNotificationDispatcher());
    }

    public ScrimReminderService(ScrimService scrimService,
                                CalendarAdapter calendarAdapter,
                                NotificadorFactory notificadorFactory,
                                NotificationDispatcher dispatcher) {
        this.scrimService = scrimService;
        this.calendarAdapter = calendarAdapter;
        this.notificadorFactory = notificadorFactory;
        this.dispatcher = dispatcher;
    }

    public String generarIcal(ScrimContext scrim) {
        return calendarAdapter.generarEvento(scrim);
    }

    public List<RecordatorioScrim> procesarRecordatorios(LocalDateTime ahora, int horasAntes) {
        if (horasAntes <= 0) {
            throw new IllegalArgumentException("horasAntes debe ser mayor a cero.");
        }

        LocalDateTime limite = ahora.plusHours(horasAntes);
        NotificadorStrategy notificador = notificadorFactory.crearEmailNotificador();
        List<RecordatorioScrim> enviadosAhora = new ArrayList<>();

        for (ScrimContext scrim : scrimService.getScrims().values()) {
            if (!"CONFIRMADO".equals(scrim.getState().getNombre())) {
                continue;
            }
            if (scrim.getFechaHora().isBefore(ahora) || scrim.getFechaHora().isAfter(limite)) {
                continue;
            }

            String ical = generarIcal(scrim);
            for (Postulacion postulacion : scrim.getPostulaciones()) {
                if (!postulacion.estaAceptada()) {
                    continue;
                }

                Usuario usuario = postulacion.getUsuario();
                String key = scrim.getId() + ":" + usuario.getId() + ":" + horasAntes;
                if (!enviados.add(key)) {
                    continue;
                }

                Notificacion notificacion = new Notificacion(
                        "RECORDATORIO_SCRIM",
                        notificador.getCanal(),
                        "Recordatorio: scrim " + scrim.getJuego() + " inicia a las " + scrim.getFechaHora()
                                + "\n" + ical
                );
                dispatcher.enqueue(usuario, notificacion, notificador);
                enviadosAhora.add(new RecordatorioScrim(
                        scrim.getId(),
                        usuario.getUsername(),
                        horasAntes,
                        notificador.getCanal(),
                        ical,
                        ahora
                ));
            }
        }

        return enviadosAhora;
    }
}
