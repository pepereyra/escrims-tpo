package escrims;

import escrims.dominio.Estadistica;
import escrims.dominio.Usuario;
import escrims.dominio.enums.CanalNotificacion;
import escrims.dominio.enums.Rol;
import escrims.facade.ScrimFacade;
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
 * Tests del PATRON FACADE.
 *
 * Verifica que ScrimFacade:
 * 1. Oculta completamente los subsistemas internos (DomainEventBus, NotificadorFactory,
 *    ScrimBuilder, ScrimController) al cliente.
 * 2. Expone una API simplificada que cubre todos los casos de uso.
 * 3. Coordina correctamente los patrones internos (State, Observer, Strategy, Builder,
 *    Abstract Factory) sin que el cliente los conozca.
 */
class ScrimFacadeTest {

    private ScrimFacade facade;
    private Usuario alpha, bravo, charlie, delta;

    @BeforeEach
    void setUp() {
        // PATRON FACADE: el cliente solo instancia ScrimFacade.
        // No conoce DomainEventBus, NotificadorFactory ni ScrimController.
        facade = new ScrimFacade();

        alpha   = crearUsuario("Alpha",   "alpha@mail.com",   "Valorant", 1500, 30);
        bravo   = crearUsuario("Bravo",   "bravo@mail.com",   "Valorant", 1600, 25);
        charlie = crearUsuario("Charlie", "charlie@mail.com", "Valorant", 1450, 40);
        delta   = crearUsuario("Delta",   "delta@mail.com",   "Valorant", 1550, 35);
    }

    @Test
    @DisplayName("Facade: crearScrim devuelve un scrim en estado BUSCANDO")
    void facadeCrearScrimEstaBuscando() {
        ScrimContext scrim = crearScrim2v2();
        assertEquals("BUSCANDO", scrim.getState().getNombre());
    }

    @Test
    @DisplayName("Facade: flujo completo BUSCANDO → FINALIZADO sin conocer subsistemas")
    void facadeFlujoCompleto() {
        ScrimContext scrim = crearScrim2v2();

        // Configurar notificaciones — el cliente no sabe que usa Observer + Strategy + Factory
        facade.configurarNotificaciones(List.of(alpha, bravo), CanalNotificacion.EMAIL);
        facade.configurarNotificaciones(List.of(charlie, delta), CanalNotificacion.PUSH);

        // Postular — el cliente no sabe que usa State (BuscandoState)
        facade.postular(scrim.getId(), alpha, Rol.DUELIST);
        facade.postular(scrim.getId(), bravo, Rol.SUPPORT);
        facade.postular(scrim.getId(), charlie, Rol.DUELIST);
        facade.postular(scrim.getId(), delta, Rol.SUPPORT);
        assertEquals("LOBBY_ARMADO", scrim.getState().getNombre());

        // Confirmar — el cliente no sabe que usa State (LobbyArmadoState)
        facade.confirmar(scrim.getId(), alpha);
        facade.confirmar(scrim.getId(), bravo);
        facade.confirmar(scrim.getId(), charlie);
        facade.confirmar(scrim.getId(), delta);
        assertEquals("CONFIRMADO", scrim.getState().getNombre());

        // Iniciar y finalizar
        facade.iniciar(scrim.getId());
        assertEquals("EN_JUEGO", scrim.getState().getNombre());

        facade.finalizar(scrim.getId());
        assertEquals("FINALIZADO", scrim.getState().getNombre());
    }

    @Test
    @DisplayName("Facade: registrar estadísticas y determinar MVP")
    void facadeRegistrarEstadisticasYMvp() {
        ScrimContext scrim = crearScrim2v2();
        flujoCompleto(scrim);

        Map<Usuario, int[]> resultados = new HashMap<>();
        resultados.put(alpha,   new int[]{10, 3, 5});  // KDA = 5.0
        resultados.put(bravo,   new int[]{5,  4, 8});  // KDA = 3.25
        resultados.put(charlie, new int[]{8,  2, 6});  // KDA = 7.0 ← MVP
        resultados.put(delta,   new int[]{3,  6, 4});  // KDA = 1.17

        List<Estadistica> stats = facade.registrarEstadisticas(scrim.getId(), resultados);
        Estadistica mvp = stats.stream().filter(Estadistica::isMvp).findFirst().orElse(null);

        assertNotNull(mvp);
        assertEquals("Charlie", mvp.getUsuario().getUsername());
    }

    @Test
    @DisplayName("Facade: cancelar scrim en estado BUSCANDO")
    void facadeCancelarEnBuscando() {
        ScrimContext scrim = crearScrim2v2();
        facade.cancelar(scrim.getId());
        assertEquals("CANCELADO", scrim.getState().getNombre());
    }

    @Test
    @DisplayName("Facade: operación inválida en estado FINALIZADO lanza excepción")
    void facadeOperacionInvalidaEnFinalizado() {
        ScrimContext scrim = crearScrim2v2();
        flujoCompleto(scrim);
        assertThrows(IllegalStateException.class,
                () -> facade.postular(scrim.getId(), alpha, Rol.DUELIST));
    }

    @Test
    @DisplayName("Facade: getScrim devuelve el contexto correcto")
    void facadeGetScrimDevuelveContexto() {
        ScrimContext scrim = crearScrim2v2();
        ScrimContext recuperado = facade.getScrim(scrim.getId());
        assertEquals(scrim.getId(), recuperado.getId());
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private ScrimContext crearScrim2v2() {
        return facade.crearScrim(
                "Valorant", "2v2", "SA",
                1400, 1700, 80,
                LocalDateTime.now().plusHours(2), 30, 4
        );
    }

    private void flujoCompleto(ScrimContext scrim) {
        facade.postular(scrim.getId(), alpha, Rol.DUELIST);
        facade.postular(scrim.getId(), bravo, Rol.SUPPORT);
        facade.postular(scrim.getId(), charlie, Rol.DUELIST);
        facade.postular(scrim.getId(), delta, Rol.SUPPORT);
        facade.confirmar(scrim.getId(), alpha);
        facade.confirmar(scrim.getId(), bravo);
        facade.confirmar(scrim.getId(), charlie);
        facade.confirmar(scrim.getId(), delta);
        facade.iniciar(scrim.getId());
        facade.finalizar(scrim.getId());
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
