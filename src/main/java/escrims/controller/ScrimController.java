package escrims.controller;

import escrims.dominio.Estadistica;
import escrims.dominio.Usuario;
import escrims.dominio.enums.CanalNotificacion;
import escrims.dominio.enums.Rol;
import escrims.factory.NotificadorFactory;
import escrims.observer.DomainEventBus;
import escrims.observer.NotificationSubscriber;
import escrims.state.ScrimBuilder;
import escrims.state.ScrimContext;
import escrims.strategy.NotificadorStrategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GRASP Controller - Coordina los casos de uso del sistema de scrims.
 * No contiene lógica de negocio: delega en ScrimContext (State) y DomainEventBus (Observer).
 * GRASP High Cohesion: solo coordina, no implementa reglas de negocio.
 * GRASP Low Coupling: depende de abstracciones (NotificadorStrategy, ScrimState, NotificadorFactory).
 * SOLID DIP: recibe NotificadorFactory por inyección → no conoce Dev ni Prod.
 */
public class ScrimController {

    private final Map<UUID, ScrimContext> scrims;
    private final DomainEventBus eventBus;
    private final NotificadorFactory notificadorFactory;

    /**
     * Constructor principal: recibe la fábrica de notificadores por inyección de dependencia.
     * PATRON ABSTRACT FACTORY: el controller no sabe si está en DEV o PROD.
     */
    public ScrimController(DomainEventBus eventBus, NotificadorFactory notificadorFactory) {
        this.scrims = new HashMap<>();
        this.eventBus = eventBus;
        this.notificadorFactory = notificadorFactory;
    }

    /**
     * Caso de uso: Crear un nuevo Scrim.
     * PATRON BUILDER: delega la construcción y validación en ScrimBuilder.
     * GRASP Creator: ScrimController registra y gestiona el ScrimContext resultante.
     * GRASP Low Coupling: no instancia ScrimContext directamente → si cambia el constructor,
     *                     solo cambia ScrimBuilder, no este método.
     */
    public ScrimContext crearScrim(String juego, String formato, String region,
                                   int rangoMin, int rangoMax, int latenciaMax,
                                   LocalDateTime fechaHora, int duracionMinutos, int cuposTotales) {
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
        System.out.println("[ScrimController] Scrim creado: " + scrim);
        return scrim;
    }

    /**
     * Caso de uso: Postular un usuario a un scrim.
     */
    public void postular(UUID scrimId, Usuario usuario, Rol rol) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.postular(usuario, rol);
    }

    /**
     * Caso de uso: Confirmar asistencia de un usuario.
     */
    public void confirmar(UUID scrimId, Usuario usuario) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.confirmar(usuario);
    }

    /**
     * Caso de uso: Iniciar el scrim (solo cuando todos confirmaron).
     */
    public void iniciar(UUID scrimId) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.iniciar();
    }

    /**
     * Caso de uso: Finalizar el scrim.
     */
    public void finalizar(UUID scrimId) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.finalizar();
    }

    /**
     * Caso de uso: Cancelar el scrim.
     */
    public void cancelar(UUID scrimId) {
        ScrimContext scrim = getScrim(scrimId);
        scrim.cancelar();
    }

    /**
     * Caso de uso: Registrar estadísticas post-scrim.
     * GRASP Expert: ScrimController conoce el scrim y puede validar que esté finalizado.
     */
    public List<Estadistica> registrarEstadisticas(UUID scrimId, Map<Usuario, int[]> resultados) {
        ScrimContext scrim = getScrim(scrimId);
        if (!scrim.getState().getNombre().equals("FINALIZADO")) {
            throw new IllegalStateException("Solo se pueden registrar estadísticas de scrims finalizados.");
        }
        List<Estadistica> estadisticas = new ArrayList<>();
        double mejorKda = -1;
        Estadistica mvpCandidate = null;

        for (Map.Entry<Usuario, int[]> entry : resultados.entrySet()) {
            Estadistica est = new Estadistica(entry.getKey());
            int[] kda = entry.getValue(); // [kills, deaths, assists]
            est.setKills(kda[0]);
            est.setDeaths(kda[1]);
            est.setAssists(kda[2]);
            estadisticas.add(est);
            if (est.getKda() > mejorKda) {
                mejorKda = est.getKda();
                mvpCandidate = est;
            }
        }
        if (mvpCandidate != null) {
            mvpCandidate.setMvp(true);
        }
        System.out.println("[ScrimController] Estadísticas registradas para scrim " + scrimId);
        estadisticas.forEach(System.out::println);
        return estadisticas;
    }

    /**
     * Configura el suscriptor de notificaciones para un scrim.
     * PATRON ABSTRACT FACTORY: delega la creación del notificador en la fábrica inyectada.
     * El controller no conoce ni instancia ningún notificador concreto.
     * SOLID DIP: depende de NotificadorFactory (abstracción), no de Email/Push/Discord concretos.
     * SOLID OCP: agregar un nuevo canal no requiere modificar este método.
     */
    public NotificationSubscriber configurarNotificaciones(List<Usuario> destinatarios,
                                                            CanalNotificacion canal) {
        NotificadorStrategy estrategia = switch (canal) {
            case EMAIL   -> notificadorFactory.crearEmailNotificador();
            case PUSH    -> notificadorFactory.crearPushNotificador();
            case DISCORD -> notificadorFactory.crearDiscordNotificador();
        };
        NotificationSubscriber subscriber = new NotificationSubscriber(
                new ArrayList<>(destinatarios), estrategia, canal);
        eventBus.subscribe(subscriber);
        return subscriber;
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
