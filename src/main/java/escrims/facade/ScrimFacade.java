package escrims.facade;

import escrims.controller.ScrimController;
import escrims.dominio.Estadistica;
import escrims.dominio.Usuario;
import escrims.dominio.enums.CanalNotificacion;
import escrims.dominio.enums.Rol;
import escrims.factory.DevNotificadorFactory;
import escrims.factory.NotificadorFactory;
import escrims.observer.DomainEventBus;
import escrims.state.ScrimContext;

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
 * ScrimBuilder (Builder), ScrimController (GRASP Controller) y los estados concretos
 * (State). El cliente (Main, tests de integración) no debería conocer ni instanciar
 * ninguna de estas clases directamente.
 *
 * Cómo se aplica:
 * ScrimFacade encapsula la creación y configuración de todos los subsistemas internos
 * (DomainEventBus, NotificadorFactory, ScrimController) y expone únicamente los
 * métodos de alto nivel que el cliente necesita: crearScrim(), postular(), confirmar(),
 * iniciar(), finalizar(), cancelar(), configurarNotificaciones(), registrarEstadisticas().
 *
 * El cliente (Main) solo conoce ScrimFacade. No conoce DomainEventBus, NotificadorFactory,
 * ScrimBuilder ni ScrimController. Si el subsistema interno cambia (ej: se agrega un nuevo
 * tipo de fábrica o se cambia el bus de eventos), el cliente no se ve afectado.
 *
 * Relación con otros patrones:
 * - Internamente usa ScrimController (GRASP Controller) para coordinar los casos de uso.
 * - ScrimController usa ScrimBuilder (Builder) para crear scrims.
 * - ScrimController usa NotificadorFactory (Abstract Factory) para crear notificadores.
 * - ScrimContext usa DomainEventBus (Observer) para publicar eventos de dominio.
 * - Los estados concretos (State) implementan la lógica de cada fase del scrim.
 */
public class ScrimFacade {

    // ── Subsistemas internos — ocultos al cliente ──────────────────────────────
    private final ScrimController controller;

    /**
     * Constructor por defecto: usa DevNotificadorFactory (entorno de desarrollo).
     * El cliente no necesita saber qué fábrica se usa ni cómo se configura el bus.
     */
    public ScrimFacade() {
        DomainEventBus eventBus = new DomainEventBus();
        NotificadorFactory factory = new DevNotificadorFactory();
        this.controller = new ScrimController(eventBus, factory);
    }

    /**
     * Constructor con fábrica inyectada: permite usar ProdNotificadorFactory en producción.
     * Útil para tests de integración o para cambiar el entorno sin modificar el cliente.
     */
    public ScrimFacade(NotificadorFactory factory) {
        DomainEventBus eventBus = new DomainEventBus();
        this.controller = new ScrimController(eventBus, factory);
    }

    // ── API simplificada para el cliente ───────────────────────────────────────

    /**
     * Crea un nuevo scrim con los parámetros dados.
     * Internamente usa ScrimBuilder para construir y validar el ScrimContext.
     */
    public ScrimContext crearScrim(String juego, String formato, String region,
                                   int rangoMin, int rangoMax, int latenciaMax,
                                   LocalDateTime fechaHora, int duracionMinutos, int cuposTotales) {
        return controller.crearScrim(juego, formato, region,
                rangoMin, rangoMax, latenciaMax,
                fechaHora, duracionMinutos, cuposTotales);
    }

    /**
     * Postula un usuario al scrim con el rol indicado.
     * Delega en el estado actual del ScrimContext (patrón State).
     */
    public void postular(UUID scrimId, Usuario usuario, Rol rol) {
        controller.postular(scrimId, usuario, rol);
    }

    /**
     * Confirma la asistencia de un usuario al scrim.
     */
    public void confirmar(UUID scrimId, Usuario usuario) {
        controller.confirmar(scrimId, usuario);
    }

    /**
     * Inicia el scrim (requiere estado CONFIRMADO).
     */
    public void iniciar(UUID scrimId) {
        controller.iniciar(scrimId);
    }

    /**
     * Finaliza el scrim (requiere estado EN_JUEGO).
     */
    public void finalizar(UUID scrimId) {
        controller.finalizar(scrimId);
    }

    /**
     * Cancela el scrim (disponible en BUSCANDO y LOBBY_ARMADO).
     */
    public void cancelar(UUID scrimId) {
        controller.cancelar(scrimId);
    }

    /**
     * Configura el canal de notificación para una lista de usuarios.
     * Internamente usa NotificadorFactory (Abstract Factory) y DomainEventBus (Observer).
     * El cliente no necesita saber cómo se crean los notificadores ni cómo se suscriben.
     */
    public void configurarNotificaciones(List<Usuario> destinatarios, CanalNotificacion canal) {
        controller.configurarNotificaciones(destinatarios, canal);
    }

    /**
     * Registra las estadísticas post-scrim y determina el MVP.
     * Solo disponible en estado FINALIZADO.
     */
    public List<Estadistica> registrarEstadisticas(UUID scrimId, Map<Usuario, int[]> resultados) {
        return controller.registrarEstadisticas(scrimId, resultados);
    }

    /**
     * Devuelve el ScrimContext de un scrim por su ID.
     * Útil para consultar el estado actual sin exponer el controller.
     */
    public ScrimContext getScrim(UUID scrimId) {
        return controller.getScrim(scrimId);
    }
}
