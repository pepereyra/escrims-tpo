package escrims.service;

import escrims.domain.command.CambiarRolCommand;
import escrims.domain.command.IntercambiarRolesCommand;
import escrims.domain.command.MoverASuplenteCommand;
import escrims.domain.command.ReactivarTitularCommand;
import escrims.domain.command.ScrimCommand;
import escrims.domain.model.Estadistica;
import escrims.domain.model.Notificacion;
import escrims.domain.model.RecordatorioScrim;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimBuilder;
import escrims.domain.state.ScrimContext;
import escrims.infra.calendar.ICalCalendarAdapter;
import escrims.infra.events.DomainEventBus;
import escrims.infra.events.NotificationSubscriber;
import escrims.infra.notification.NotificadorFactory;
import escrims.infra.notification.NotificadorStrategy;
import escrims.infra.notification.QueuedNotificationDispatcher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CAPA SERVICE - Orquesta los casos de uso del sistema de scrims.
 *
 * Responsabilidad:
 * Coordina operaciones de aplicación como crear scrims, postular usuarios,
 * confirmar asistencia, iniciar/finalizar/cancelar scrims, configurar
 * notificaciones y registrar estadísticas.
 *
 * GRASP Controller:
 * actúa como coordinador de los casos de uso, delegando las reglas de negocio
 * en el dominio: ScrimContext, ScrimState, MatchmakingStrategy y entidades.
 *
 * SOLID SRP:
 * concentra la lógica de aplicación, evitando que el controller tenga lógica
 * de negocio o de orquestación compleja.
 *
 * SOLID DIP:
 * depende de abstracciones como NotificadorFactory y NotificadorStrategy.
 */
public class ScrimService {

    private final ScrimRepository scrimRepository;
    private final DomainEventBus eventBus;
    private final NotificadorFactory notificadorFactory;
    private final ScrimSchedulerService schedulerService;
    private final ScrimReminderService reminderService;
    private final QueuedNotificationDispatcher notificationDispatcher;

    public ScrimService(DomainEventBus eventBus, NotificadorFactory notificadorFactory) {
        this(eventBus, notificadorFactory, new InMemoryScrimRepository());
    }

    public ScrimService(DomainEventBus eventBus,
                        NotificadorFactory notificadorFactory,
                        ScrimRepository scrimRepository) {
        this.scrimRepository = scrimRepository;
        this.eventBus = eventBus;
        this.notificadorFactory = notificadorFactory;
        this.schedulerService = new ScrimSchedulerService(this);
        this.reminderService = new ScrimReminderService(this, new ICalCalendarAdapter(), notificadorFactory);
        this.notificationDispatcher = new QueuedNotificationDispatcher();
    }

    public ScrimContext crearScrim(String juego,
                                   String formato,
                                   String region,
                                   int rangoMin,
                                   int rangoMax,
                                   int latenciaMax,
                                   LocalDateTime fechaHora,
                                   int duracionMinutos,
                                   int cuposTotales) {
        return crearScrim(juego, formato, region, rangoMin, rangoMax, latenciaMax, fechaHora, duracionMinutos,
                cuposTotales, "CASUAL");
    }

    public ScrimContext crearScrim(String juego,
                                   String formato,
                                   String region,
                                   int rangoMin,
                                   int rangoMax,
                                   int latenciaMax,
                                   LocalDateTime fechaHora,
                                   int duracionMinutos,
                                   int cuposTotales,
                                   String modalidad) {

        ScrimContext scrim = new ScrimBuilder(eventBus)
                .juego(juego)
                .formato(formato)
                .region(region)
                .rango(rangoMin, rangoMax)
                .latenciaMax(latenciaMax)
                .fechaHora(fechaHora)
                .duracionMinutos(duracionMinutos)
                .cuposTotales(cuposTotales)
                .modalidad(modalidad)
                .build();

        scrimRepository.save(scrim);

        System.out.println("[ScrimService] Scrim creado: " + scrim);

        return scrim;
    }

    public void postular(UUID scrimId, Usuario usuario, Rol rol) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.postular(usuario, rol);
        scrimRepository.save(scrim);
    }

    public void confirmar(UUID scrimId, Usuario usuario) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.confirmar(usuario);
        scrimRepository.save(scrim);
    }

    public void iniciar(UUID scrimId) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.iniciar();
        scrimRepository.save(scrim);
    }

    public void finalizar(UUID scrimId) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.finalizar();
        scrimRepository.save(scrim);
    }

    public void cancelar(UUID scrimId) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.cancelar();
        scrimRepository.save(scrim);
    }

    public void cambiarRol(UUID scrimId, Usuario usuario, Rol nuevoRol) {
        ScrimContext scrim = getScrim(scrimId);
        ejecutar(scrim, new CambiarRolCommand(scrim, usuario, nuevoRol));
    }

    public void intercambiarRoles(UUID scrimId, Usuario usuarioA, Usuario usuarioB) {
        ScrimContext scrim = getScrim(scrimId);
        ejecutar(scrim, new IntercambiarRolesCommand(scrim, usuarioA, usuarioB));
    }

    public void moverASuplente(UUID scrimId, Usuario usuario) {
        ScrimContext scrim = getScrim(scrimId);
        ejecutar(scrim, new MoverASuplenteCommand(scrim, usuario));
        notificarListaEspera(scrim, usuario);
    }

    public void reactivarTitular(UUID scrimId, Usuario usuario) {
        ScrimContext scrim = getScrim(scrimId);
        ejecutar(scrim, new ReactivarTitularCommand(scrim, usuario));
    }

    private void notificarListaEspera(ScrimContext scrim, Usuario usuarioMovido) {
        NotificadorStrategy notificador = notificadorFactory.crearEmailNotificador();
        scrim.getPostulaciones().stream()
                .filter(postulacion -> "SUPLENTE".equals(postulacion.getEstado().getNombre()))
                .map(postulacion -> postulacion.getUsuario())
                .forEach(usuario -> {
                    Notificacion notificacion = new Notificacion(
                            "CUPO_LIBERADO",
                            notificador.getCanal(),
                            "Se libero un cupo en el scrim " + scrim.getId()
                                    + " porque " + usuarioMovido.getUsername() + " paso a suplente."
                    );
                    notificationDispatcher.enqueue(usuario, notificacion, notificador);
                });
    }

    private void ejecutar(ScrimContext scrim, ScrimCommand command) {
        command.ejecutar();
        scrimRepository.save(scrim);
    }

    public List<Estadistica> registrarEstadisticas(UUID scrimId,
                                                   Map<Usuario, int[]> resultados) {

        ScrimContext scrim = getScrim(scrimId);

        if (!scrim.getState().getNombre().equals("FINALIZADO")) {
            throw new IllegalStateException(
                    "Solo se pueden registrar estadísticas de scrims finalizados."
            );
        }

        List<Estadistica> estadisticas = new ArrayList<>();

        double mejorKda = -1;
        Estadistica mvpCandidate = null;

        for (Map.Entry<Usuario, int[]> entry : resultados.entrySet()) {
            Estadistica estadistica = new Estadistica(entry.getKey());

            int[] kda = entry.getValue();

            estadistica.setKills(kda[0]);
            estadistica.setDeaths(kda[1]);
            estadistica.setAssists(kda[2]);

            estadisticas.add(estadistica);

            if (estadistica.getKda() > mejorKda) {
                mejorKda = estadistica.getKda();
                mvpCandidate = estadistica;
            }
        }

        if (mvpCandidate != null) {
            mvpCandidate.setMvp(true);
        }

        scrim.getEstadisticas().clear();
        scrim.getEstadisticas().addAll(estadisticas);
        scrimRepository.save(scrim);

        System.out.println("[ScrimService] Estadísticas registradas para scrim " + scrimId);
        estadisticas.forEach(System.out::println);

        return estadisticas;
    }

    public NotificationSubscriber configurarNotificacionesEmail(List<Usuario> destinatarios) {
        return configurarNotificaciones(destinatarios, notificadorFactory.crearEmailNotificador());
    }

    public NotificationSubscriber configurarNotificacionesPush(List<Usuario> destinatarios) {
        return configurarNotificaciones(destinatarios, notificadorFactory.crearPushNotificador());
    }

    public NotificationSubscriber configurarNotificacionesDiscord(List<Usuario> destinatarios) {
        return configurarNotificaciones(destinatarios, notificadorFactory.crearDiscordNotificador());
    }

    public NotificationSubscriber configurarNotificaciones(List<Usuario> destinatarios,
                                                            NotificadorStrategy estrategia) {
        NotificationSubscriber subscriber = new NotificationSubscriber(
                new ArrayList<>(destinatarios),
                estrategia
        );

        eventBus.subscribe(subscriber);

        return subscriber;
    }

    public int procesarScrimsProgramados() {
        return schedulerService.procesarScrimsProgramados();
    }

    public int procesarScrimsProgramados(LocalDateTime ahora) {
        return schedulerService.procesarScrimsProgramados(ahora);
    }

    public String generarIcal(UUID scrimId) {
        return reminderService.generarIcal(getScrim(scrimId));
    }

    public List<RecordatorioScrim> procesarRecordatorios(LocalDateTime ahora, int horasAntes) {
        return reminderService.procesarRecordatorios(ahora, horasAntes);
    }

    public ScrimContext getScrim(UUID scrimId) {
        ScrimContext scrim = scrimRepository.findById(scrimId).orElse(null);

        if (scrim == null) {
            throw new IllegalArgumentException("Scrim no encontrado: " + scrimId);
        }

        return scrim;
    }

    public Map<UUID, ScrimContext> getScrims() {
        Map<UUID, ScrimContext> scrims = new LinkedHashMap<>();
        for (ScrimContext scrim : scrimRepository.findAll()) {
            scrims.put(scrim.getId(), scrim);
        }
        return scrims;
    }
}
