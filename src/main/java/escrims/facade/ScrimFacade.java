package escrims.facade;

import escrims.controller.ScrimController;
import escrims.domain.model.Estadistica;
import escrims.domain.model.RecordatorioScrim;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;
import escrims.infra.events.DomainEventBus;
import escrims.infra.notification.DevNotificadorFactory;
import escrims.infra.notification.NotificadorFactory;
import escrims.infra.notification.NotificadorStrategy;
import escrims.service.ScrimService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PATRON FACADE — Punto de entrada simplificado al subsistema de eScrims.
 *
 * Problema que resuelve:
 * El subsistema de scrims está compuesto por múltiples clases con responsabilidades
 * específicas: DomainEventBus (Observer), NotificadorFactory (Abstract Factory),
 * ScrimService (capa de aplicación), ScrimController (GRASP Controller),
 * ScrimBuilder (Builder), MatchmakingStrategy (Strategy) y los estados concretos
 * (State). El cliente (Main, tests de integración) no debería conocer ni instanciar
 * ninguna de estas clases directamente.
 *
 * Cómo se aplica:
 * ScrimFacade encapsula la creación y configuración de todos los subsistemas internos
 * (DomainEventBus, NotificadorFactory, ScrimService y ScrimController) y expone
 * únicamente los métodos de alto nivel que el cliente necesita:
 * crearScrim(), postular(), confirmar(), iniciar(), finalizar(), cancelar(),
 * configurarNotificacionesEmail(), configurarNotificacionesPush(),
 * configurarNotificacionesDiscord(), procesarScrimsProgramados(),
 * registrarEstadisticas().
 *
 * El cliente (Main) solo conoce ScrimFacade. No conoce DomainEventBus,
 * NotificadorFactory, ScrimService, ScrimBuilder ni ScrimController.
 * Si el subsistema interno cambia, el cliente no se ve afectado.
 *
 * Relación con otros patrones:
 * - Internamente usa ScrimController como punto de entrada de casos de uso.
 * - ScrimController delega en ScrimService.
 * - ScrimService usa ScrimBuilder para crear scrims.
 * - ScrimBuilder configura MatchmakingStrategy por defecto.
 * - ScrimService usa NotificadorFactory para crear notificadores.
 * - ScrimContext usa DomainEventBus para publicar eventos de dominio.
 * - Los estados concretos implementan el ciclo de vida del scrim mediante State.
 */
public class ScrimFacade {

    private final ScrimController controller;

    public ScrimFacade(ScrimController controller) {
        this.controller = controller;
    }

    public ScrimFacade() {
        DomainEventBus eventBus = new DomainEventBus();
        NotificadorFactory factory = new DevNotificadorFactory();
        ScrimService service = new ScrimService(eventBus, factory);
        this.controller = new ScrimController(service);
    }

    public ScrimFacade(NotificadorFactory factory) {
        DomainEventBus eventBus = new DomainEventBus();
        ScrimService service = new ScrimService(eventBus, factory);
        this.controller = new ScrimController(service);
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

        return controller.crearScrim(
                juego,
                formato,
                region,
                rangoMin,
                rangoMax,
                latenciaMax,
                fechaHora,
                duracionMinutos,
                cuposTotales,
                modalidad
        );
    }

    public void postular(UUID scrimId, Usuario usuario, Rol rol) {
        controller.postular(scrimId, usuario, rol);
    }

    public void confirmar(UUID scrimId, Usuario usuario) {
        controller.confirmar(scrimId, usuario);
    }

    public void iniciar(UUID scrimId) {
        controller.iniciar(scrimId);
    }

    public void finalizar(UUID scrimId) {
        controller.finalizar(scrimId);
    }

    public void cancelar(UUID scrimId) {
        controller.cancelar(scrimId);
    }

    public void cambiarRol(UUID scrimId, Usuario usuario, Rol nuevoRol) {
        controller.cambiarRol(scrimId, usuario, nuevoRol);
    }

    public void intercambiarRoles(UUID scrimId, Usuario usuarioA, Usuario usuarioB) {
        controller.intercambiarRoles(scrimId, usuarioA, usuarioB);
    }

    public void moverASuplente(UUID scrimId, Usuario usuario) {
        controller.moverASuplente(scrimId, usuario);
    }

    public void reactivarTitular(UUID scrimId, Usuario usuario) {
        controller.reactivarTitular(scrimId, usuario);
    }

    public void deshacerUltimoComando(UUID scrimId) {
        controller.deshacerUltimoComando(scrimId);
    }

    public void configurarNotificacionesEmail(List<Usuario> destinatarios) {
        controller.configurarNotificacionesEmail(destinatarios);
    }

    public void configurarNotificacionesPush(List<Usuario> destinatarios) {
        controller.configurarNotificacionesPush(destinatarios);
    }

    public void configurarNotificacionesDiscord(List<Usuario> destinatarios) {
        controller.configurarNotificacionesDiscord(destinatarios);
    }

    public void configurarNotificaciones(List<Usuario> destinatarios,
                                          NotificadorStrategy estrategia) {

        controller.configurarNotificaciones(destinatarios, estrategia);
    }

    public int procesarScrimsProgramados() {
        return controller.procesarScrimsProgramados();
    }

    public int procesarScrimsProgramados(LocalDateTime ahora) {
        return controller.procesarScrimsProgramados(ahora);
    }

    public String generarIcal(UUID scrimId) {
        return controller.generarIcal(scrimId);
    }

    public List<RecordatorioScrim> procesarRecordatorios(LocalDateTime ahora, int horasAntes) {
        return controller.procesarRecordatorios(ahora, horasAntes);
    }

    public List<Estadistica> registrarEstadisticas(UUID scrimId,
                                                   Map<Usuario, int[]> resultados) {

        return controller.registrarEstadisticas(scrimId, resultados);
    }

    public ScrimContext getScrim(UUID scrimId) {
        return controller.getScrim(scrimId);
    }

    public List<ScrimContext> listarScrims() {
        return controller.listarScrims();
    }
}
