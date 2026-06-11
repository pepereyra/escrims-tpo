package escrims;

import escrims.controller.ScrimController;
import escrims.dominio.Estadistica;
import escrims.dominio.Usuario;
import escrims.dominio.enums.CanalNotificacion;
import escrims.dominio.enums.Rol;
import escrims.factory.DevNotificadorFactory;
import escrims.observer.DomainEventBus;
import escrims.state.ScrimContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de tests del flujo completo del Scrim.
 * Verifica el patrón State, Observer y Strategy integrados.
 */
class ScrimFlowTest {

    private ScrimController controller;
    private DomainEventBus eventBus;
    private Usuario alpha, bravo, charlie, delta;

    @BeforeEach
    void setUp() {
        eventBus = new DomainEventBus();
        // PATRON ABSTRACT FACTORY: en tests usamos DevNotificadorFactory (sin servicios externos)
        controller = new ScrimController(eventBus, new DevNotificadorFactory());

        alpha   = crearUsuario("Alpha",   "alpha@mail.com",   "Valorant", 1500, 30);
        bravo   = crearUsuario("Bravo",   "bravo@mail.com",   "Valorant", 1600, 25);
        charlie = crearUsuario("Charlie", "charlie@mail.com", "Valorant", 1450, 40);
        delta   = crearUsuario("Delta",   "delta@mail.com",   "Valorant", 1550, 35);
    }

    // ── Tests de estado inicial ──────────────────────────────────────

    @Test
    @DisplayName("Scrim recién creado debe estar en estado BUSCANDO")
    void scrimNuevoEstaBuscando() {
        ScrimContext scrim = crearScrim2v2();
        assertEquals("BUSCANDO", scrim.getState().getNombre());
    }

    @Test
    @DisplayName("Scrim recién creado debe tener 4 cupos disponibles")
    void scrimNuevoTiene4Cupos() {
        ScrimContext scrim = crearScrim2v2();
        assertEquals(4, scrim.cuposDisponibles());
    }

    // ── Tests de postulación ─────────────────────────────────────────

    @Test
    @DisplayName("Postular 4 jugadores debe transicionar a LOBBY_ARMADO")
    void postular4JugadoresTransicionaALobbyArmado() {
        ScrimContext scrim = crearScrim2v2();
        postularTodos(scrim);
        assertEquals("LOBBY_ARMADO", scrim.getState().getNombre());
    }

    @Test
    @DisplayName("No se puede postular un usuario sin email verificado")
    void noSePuedePostularSinVerificar() {
        ScrimContext scrim = crearScrim2v2();
        Usuario noVerificado = new Usuario("Ghost", "ghost@mail.com", "hash", "SA");
        // No llama a verificarEmail()
        assertThrows(IllegalArgumentException.class,
                () -> controller.postular(scrim.getId(), noVerificado, Rol.DUELIST));
    }

    @Test
    @DisplayName("No se puede postular el mismo usuario dos veces")
    void noSePuedePostularDosVeces() {
        ScrimContext scrim = crearScrim2v2();
        controller.postular(scrim.getId(), alpha, Rol.DUELIST);
        assertThrows(IllegalStateException.class,
                () -> controller.postular(scrim.getId(), alpha, Rol.SUPPORT));
    }

    @Test
    @DisplayName("No se puede postular con rango fuera del rango permitido")
    void noSePuedePostularConRangoFueraDeRango() {
        ScrimContext scrim = crearScrim2v2();
        Usuario fueraDeRango = crearUsuario("Noob", "noob@mail.com", "Valorant", 500, 30);
        assertThrows(IllegalArgumentException.class,
                () -> controller.postular(scrim.getId(), fueraDeRango, Rol.DUELIST));
    }

    @Test
    @DisplayName("No se puede postular con latencia mayor al máximo")
    void noSePuedePostularConAltaLatencia() {
        ScrimContext scrim = crearScrim2v2();
        Usuario altaLatencia = crearUsuario("Lagger", "lag@mail.com", "Valorant", 1500, 200);
        assertThrows(IllegalArgumentException.class,
                () -> controller.postular(scrim.getId(), altaLatencia, Rol.DUELIST));
    }

    @Test
    @DisplayName("No se puede postular en estado LOBBY_ARMADO")
    void noSePuedePostularEnLobbyArmado() {
        ScrimContext scrim = crearScrim2v2();
        postularTodos(scrim);
        assertEquals("LOBBY_ARMADO", scrim.getState().getNombre());
        Usuario extra = crearUsuario("Extra", "extra@mail.com", "Valorant", 1500, 30);
        assertThrows(IllegalStateException.class,
                () -> controller.postular(scrim.getId(), extra, Rol.DUELIST));
    }

    // ── Tests de confirmación ────────────────────────────────────────

    @Test
    @DisplayName("Confirmar todos los jugadores transiciona a CONFIRMADO")
    void confirmarTodosTransicionaAConfirmado() {
        ScrimContext scrim = crearScrim2v2();
        postularTodos(scrim);
        confirmarTodos(scrim);
        assertEquals("CONFIRMADO", scrim.getState().getNombre());
    }

    @Test
    @DisplayName("No se puede confirmar en estado BUSCANDO")
    void noSePuedeConfirmarEnBuscando() {
        ScrimContext scrim = crearScrim2v2();
        assertThrows(IllegalStateException.class,
                () -> controller.confirmar(scrim.getId(), alpha));
    }

    @Test
    @DisplayName("No se puede confirmar un usuario que no está en el scrim")
    void noSePuedeConfirmarUsuarioNoPostulado() {
        ScrimContext scrim = crearScrim2v2();
        postularTodos(scrim);
        Usuario externo = crearUsuario("Externo", "ext@mail.com", "Valorant", 1500, 30);
        assertThrows(IllegalArgumentException.class,
                () -> controller.confirmar(scrim.getId(), externo));
    }

    // ── Tests de inicio ──────────────────────────────────────────────

    @Test
    @DisplayName("Iniciar scrim confirmado transiciona a EN_JUEGO")
    void iniciarScrimConfirmadoTransicionaAEnJuego() {
        ScrimContext scrim = crearScrim2v2();
        postularTodos(scrim);
        confirmarTodos(scrim);
        controller.iniciar(scrim.getId());
        assertEquals("EN_JUEGO", scrim.getState().getNombre());
    }

    @Test
    @DisplayName("No se puede iniciar en estado BUSCANDO")
    void noSePuedeIniciarEnBuscando() {
        ScrimContext scrim = crearScrim2v2();
        assertThrows(IllegalStateException.class,
                () -> controller.iniciar(scrim.getId()));
    }

    @Test
    @DisplayName("No se puede iniciar en estado LOBBY_ARMADO (faltan confirmaciones)")
    void noSePuedeIniciarEnLobbyArmado() {
        ScrimContext scrim = crearScrim2v2();
        postularTodos(scrim);
        assertThrows(IllegalStateException.class,
                () -> controller.iniciar(scrim.getId()));
    }

    // ── Tests de finalización ────────────────────────────────────────

    @Test
    @DisplayName("Finalizar scrim en juego transiciona a FINALIZADO")
    void finalizarScrimEnJuegoTransicionaAFinalizado() {
        ScrimContext scrim = crearScrim2v2();
        flujoCompleto(scrim);
        assertEquals("FINALIZADO", scrim.getState().getNombre());
    }

    @Test
    @DisplayName("No se puede finalizar en estado BUSCANDO")
    void noSePuedeFinalizarEnBuscando() {
        ScrimContext scrim = crearScrim2v2();
        assertThrows(IllegalStateException.class,
                () -> controller.finalizar(scrim.getId()));
    }

    // ── Tests de cancelación ─────────────────────────────────────────

    @Test
    @DisplayName("Cancelar en estado BUSCANDO transiciona a CANCELADO")
    void cancelarEnBuscandoTransicionaACancelado() {
        ScrimContext scrim = crearScrim2v2();
        controller.cancelar(scrim.getId());
        assertEquals("CANCELADO", scrim.getState().getNombre());
    }

    @Test
    @DisplayName("Cancelar en estado LOBBY_ARMADO transiciona a CANCELADO")
    void cancelarEnLobbyArmadoTransicionaACancelado() {
        ScrimContext scrim = crearScrim2v2();
        postularTodos(scrim);
        controller.cancelar(scrim.getId());
        assertEquals("CANCELADO", scrim.getState().getNombre());
    }

    @Test
    @DisplayName("No se puede cancelar en estado EN_JUEGO")
    void noSePuedeCancelarEnJuego() {
        ScrimContext scrim = crearScrim2v2();
        postularTodos(scrim);
        confirmarTodos(scrim);
        controller.iniciar(scrim.getId());
        assertThrows(IllegalStateException.class,
                () -> controller.cancelar(scrim.getId()));
    }

    @Test
    @DisplayName("No se puede operar en estado FINALIZADO")
    void noSePuedeOperarEnFinalizado() {
        ScrimContext scrim = crearScrim2v2();
        flujoCompleto(scrim);
        assertThrows(IllegalStateException.class,
                () -> controller.postular(scrim.getId(), alpha, Rol.DUELIST));
    }

    // ── Tests de estadísticas ────────────────────────────────────────

    @Test
    @DisplayName("Las estadísticas solo se pueden registrar en estado FINALIZADO")
    void estadisticasSoloEnFinalizado() {
        ScrimContext scrim = crearScrim2v2();
        postularTodos(scrim);
        confirmarTodos(scrim);
        controller.iniciar(scrim.getId());
        // Aún en EN_JUEGO
        Map<Usuario, int[]> resultados = new HashMap<>();
        resultados.put(alpha, new int[]{5, 2, 3});
        assertThrows(IllegalStateException.class,
                () -> controller.registrarEstadisticas(scrim.getId(), resultados));
    }

    @Test
    @DisplayName("El MVP debe ser el jugador con mayor KDA")
    void mvpEsElDeMayorKda() {
        ScrimContext scrim = crearScrim2v2();
        flujoCompleto(scrim);

        Map<Usuario, int[]> resultados = new HashMap<>();
        resultados.put(alpha,   new int[]{10, 3, 5});  // KDA = 5.0
        resultados.put(bravo,   new int[]{5,  4, 8});  // KDA = 3.25
        resultados.put(charlie, new int[]{8,  2, 6});  // KDA = 7.0 ← MVP
        resultados.put(delta,   new int[]{3,  6, 4});  // KDA = 1.17

        List<Estadistica> stats = controller.registrarEstadisticas(scrim.getId(), resultados);
        Estadistica mvp = stats.stream().filter(Estadistica::isMvp).findFirst().orElse(null);

        assertNotNull(mvp);
        assertEquals("Charlie", mvp.getUsuario().getUsername());
    }

    // ── Tests de notificaciones ──────────────────────────────────────

    @Test
    @DisplayName("Configurar notificaciones registra suscriptores en el EventBus")
    void configurarNotificacionesRegistraSuscriptores() {
        controller.configurarNotificaciones(List.of(alpha, bravo), CanalNotificacion.EMAIL);
        controller.configurarNotificaciones(List.of(charlie, delta), CanalNotificacion.PUSH);
        assertEquals(2, eventBus.cantidadSuscriptores());
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private ScrimContext crearScrim2v2() {
        return controller.crearScrim(
                "Valorant", "2v2", "SA",
                1400, 1700, 80,
                LocalDateTime.now().plusHours(2), 30, 4
        );
    }

    private void postularTodos(ScrimContext scrim) {
        controller.postular(scrim.getId(), alpha,   Rol.DUELIST);
        controller.postular(scrim.getId(), bravo,   Rol.SUPPORT);
        controller.postular(scrim.getId(), charlie, Rol.DUELIST);
        controller.postular(scrim.getId(), delta,   Rol.SUPPORT);
    }

    private void confirmarTodos(ScrimContext scrim) {
        controller.confirmar(scrim.getId(), alpha);
        controller.confirmar(scrim.getId(), bravo);
        controller.confirmar(scrim.getId(), charlie);
        controller.confirmar(scrim.getId(), delta);
    }

    private void flujoCompleto(ScrimContext scrim) {
        postularTodos(scrim);
        confirmarTodos(scrim);
        controller.iniciar(scrim.getId());
        controller.finalizar(scrim.getId());
    }

    private static Usuario crearUsuario(String username, String email, String juego,
                                         int rango, int latencia) {
        Usuario u = new Usuario(username, email, "hash123", "SA");
        u.verificarEmail();
        u.setLatenciaPromedio(latencia);
        Map<String, Integer> rangos = new HashMap<>();
        rangos.put(juego, rango);
        u.setRangoPorJuego(rangos);
        return u;
    }
}
