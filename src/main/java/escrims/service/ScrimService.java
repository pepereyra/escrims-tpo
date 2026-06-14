package escrims.service;

import escrims.domain.model.Estadistica;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimBuilder;
import escrims.domain.state.ScrimContext;
import escrims.infra.events.DomainEventBus;
import escrims.infra.events.NotificationSubscriber;
import escrims.infra.notification.NotificadorFactory;
import escrims.infra.notification.NotificadorStrategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

    private final Map<UUID, ScrimContext> scrims;
    private final DomainEventBus eventBus;
    private final NotificadorFactory notificadorFactory;
    private final ScrimSchedulerService schedulerService;

    public ScrimService(DomainEventBus eventBus, NotificadorFactory notificadorFactory) {
        this.scrims = new HashMap<>();
        this.eventBus = eventBus;
        this.notificadorFactory = notificadorFactory;
        this.schedulerService = new ScrimSchedulerService(this);
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

        ScrimContext scrim = new ScrimBuilder(eventBus)
                .juego(juego)
                .formato(formato)
                .region(region)
                .rango(rangoMin, rangoMax)
                .latenciaMax(latenciaMax)
                .fechaHora(fechaHora)
                .duracionMinutos(duracionMinutos)
                .cuposTotales(cuposTotales)
                .build();

        scrims.put(scrim.getId(), scrim);

        System.out.println("[ScrimService] Scrim creado: " + scrim);

        return scrim;
    }

    public void postular(UUID scrimId, Usuario usuario, Rol rol) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.postular(usuario, rol);
    }

    public void confirmar(UUID scrimId, Usuario usuario) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.confirmar(usuario);
    }

    public void iniciar(UUID scrimId) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.iniciar();
    }

    public void finalizar(UUID scrimId) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.finalizar();
    }

    public void cancelar(UUID scrimId) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.cancelar();
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

    public ScrimContext getScrim(UUID scrimId) {
        ScrimContext scrim = scrims.get(scrimId);

        if (scrim == null) {
            throw new IllegalArgumentException("Scrim no encontrado: " + scrimId);
        }

        return scrim;
    }

    public Map<UUID, ScrimContext> getScrims() {
        return scrims;
    }
}
