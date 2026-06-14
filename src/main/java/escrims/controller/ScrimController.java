package escrims.controller;

import escrims.domain.model.Estadistica;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;
import escrims.infra.events.NotificationSubscriber;
import escrims.infra.notification.NotificadorStrategy;
import escrims.service.ScrimService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CAPA CONTROLLER - Punto de entrada de los casos de uso.
 *
 * Responsabilidad:
 * recibe solicitudes de la capa externa y delega la ejecución en ScrimService.
 *
 * GRASP Controller:
 * representa el primer objeto que recibe los eventos del sistema.
 *
 * SOLID SRP:
 * no contiene lógica de negocio; solo coordina la entrada hacia la capa service.
 */
public class ScrimController {

    private final ScrimService scrimService;

    public ScrimController(ScrimService scrimService) {
        this.scrimService = scrimService;
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

        return scrimService.crearScrim(
                juego,
                formato,
                region,
                rangoMin,
                rangoMax,
                latenciaMax,
                fechaHora,
                duracionMinutos,
                cuposTotales
        );
    }

    public void postular(UUID scrimId, Usuario usuario, Rol rol) {
        scrimService.postular(scrimId, usuario, rol);
    }

    public void confirmar(UUID scrimId, Usuario usuario) {
        scrimService.confirmar(scrimId, usuario);
    }

    public void iniciar(UUID scrimId) {
        scrimService.iniciar(scrimId);
    }

    public void finalizar(UUID scrimId) {
        scrimService.finalizar(scrimId);
    }

    public void cancelar(UUID scrimId) {
        scrimService.cancelar(scrimId);
    }

    public void cambiarRol(UUID scrimId, Usuario usuario, Rol nuevoRol) {
        scrimService.cambiarRol(scrimId, usuario, nuevoRol);
    }

    public void intercambiarRoles(UUID scrimId, Usuario usuarioA, Usuario usuarioB) {
        scrimService.intercambiarRoles(scrimId, usuarioA, usuarioB);
    }

    public void moverASuplente(UUID scrimId, Usuario usuario) {
        scrimService.moverASuplente(scrimId, usuario);
    }

    public List<Estadistica> registrarEstadisticas(UUID scrimId,
                                                   Map<Usuario, int[]> resultados) {

        return scrimService.registrarEstadisticas(scrimId, resultados);
    }

    public NotificationSubscriber configurarNotificacionesEmail(List<Usuario> destinatarios) {
        return scrimService.configurarNotificacionesEmail(destinatarios);
    }

    public NotificationSubscriber configurarNotificacionesPush(List<Usuario> destinatarios) {
        return scrimService.configurarNotificacionesPush(destinatarios);
    }

    public NotificationSubscriber configurarNotificacionesDiscord(List<Usuario> destinatarios) {
        return scrimService.configurarNotificacionesDiscord(destinatarios);
    }

    public NotificationSubscriber configurarNotificaciones(List<Usuario> destinatarios,
                                                            NotificadorStrategy estrategia) {

        return scrimService.configurarNotificaciones(destinatarios, estrategia);
    }

    public int procesarScrimsProgramados() {
        return scrimService.procesarScrimsProgramados();
    }

    public int procesarScrimsProgramados(LocalDateTime ahora) {
        return scrimService.procesarScrimsProgramados(ahora);
    }

    public ScrimContext getScrim(UUID scrimId) {
        return scrimService.getScrim(scrimId);
    }

    public List<ScrimContext> listarScrims() {
        return List.copyOf(scrimService.getScrims().values());
    }
}
