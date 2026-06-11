package escrims;

import escrims.dominio.Estadistica;
import escrims.dominio.Usuario;
import escrims.dominio.enums.CanalNotificacion;
import escrims.dominio.enums.Rol;
import escrims.facade.ScrimFacade;
import escrims.state.ScrimContext;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MAIN - Demuestra el flujo completo de un Scrim 2v2.
 *
 * El cliente (Main) solo conoce ScrimFacade.
 * No instancia DomainEventBus, NotificadorFactory, ScrimBuilder ni ScrimController.
 * Eso es exactamente el propósito del PATRON FACADE: simplificar el acceso al subsistema.
 *
 * Flujo:
 *   1. Crear scrim (BUSCANDO)
 *   2. Configurar notificaciones (Observer + Strategy, ocultos por Facade)
 *   3. Postular 4 jugadores → transición automática a LOBBY_ARMADO
 *   4. Confirmar todos → transición a CONFIRMADO
 *   5. Iniciar → EN_JUEGO
 *   6. Finalizar → FINALIZADO
 *   7. Registrar estadísticas y MVP
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("  ESCRIMS - Sistema de Scrims Competitivos");
        System.out.println("=".repeat(60));

        // ── 1. Facade — punto de entrada único al subsistema ─────────
        // PATRON FACADE: el cliente solo conoce ScrimFacade.
        // Internamente crea DomainEventBus, DevNotificadorFactory y ScrimController.
        ScrimFacade facade = new ScrimFacade();

        // ── 2. Crear usuarios ────────────────────────────────────────
        Usuario alpha   = crearUsuario("Alpha",   "alpha@mail.com",   "Valorant", 1500, 30);
        Usuario bravo   = crearUsuario("Bravo",   "bravo@mail.com",   "Valorant", 1600, 25);
        Usuario charlie = crearUsuario("Charlie", "charlie@mail.com", "Valorant", 1450, 40);
        Usuario delta   = crearUsuario("Delta",   "delta@mail.com",   "Valorant", 1550, 35);

        // ── 3. Crear scrim 2v2 ───────────────────────────────────────
        System.out.println("\n--- CREANDO SCRIM ---");
        // PATRON FACADE: una sola llamada oculta ScrimBuilder + validaciones + DomainEventBus
        ScrimContext scrim = facade.crearScrim(
                "Valorant", "2v2", "SA",
                1400, 1700, 80,
                LocalDateTime.now().plusHours(2), 30, 4
        );

        // ── 4. Configurar notificaciones (Observer + Strategy) ───────
        System.out.println("\n--- CONFIGURANDO NOTIFICACIONES ---");
        // PATRON FACADE: el cliente no sabe que internamente se usa NotificadorFactory
        // ni que se suscribe un NotificationSubscriber al DomainEventBus.
        facade.configurarNotificaciones(List.of(alpha, bravo), CanalNotificacion.EMAIL);
        facade.configurarNotificaciones(List.of(charlie, delta), CanalNotificacion.PUSH);

        // ── 5. Postulaciones (BUSCANDO → LOBBY_ARMADO automático) ───
        System.out.println("\n--- POSTULACIONES ---");
        facade.postular(scrim.getId(), alpha, Rol.DUELIST);
        facade.postular(scrim.getId(), bravo, Rol.SUPPORT);
        facade.postular(scrim.getId(), charlie, Rol.DUELIST);
        // La 4ta postulación completa los cupos → transición automática a LOBBY_ARMADO
        facade.postular(scrim.getId(), delta, Rol.SUPPORT);

        System.out.println("\nEstado actual: " + scrim.getState().getNombre());

        // ── 6. Confirmaciones (LOBBY_ARMADO → CONFIRMADO) ───────────
        System.out.println("\n--- CONFIRMACIONES ---");
        facade.confirmar(scrim.getId(), alpha);
        facade.confirmar(scrim.getId(), bravo);
        facade.confirmar(scrim.getId(), charlie);
        // La última confirmación → transición automática a CONFIRMADO
        facade.confirmar(scrim.getId(), delta);

        System.out.println("\nEstado actual: " + scrim.getState().getNombre());

        // ── 7. Iniciar (CONFIRMADO → EN_JUEGO) ──────────────────────
        System.out.println("\n--- INICIANDO SCRIM ---");
        facade.iniciar(scrim.getId());
        System.out.println("Estado actual: " + scrim.getState().getNombre());

        // ── 8. Finalizar (EN_JUEGO → FINALIZADO) ────────────────────
        System.out.println("\n--- FINALIZANDO SCRIM ---");
        facade.finalizar(scrim.getId());
        System.out.println("Estado actual: " + scrim.getState().getNombre());

        // ── 9. Estadísticas y MVP ────────────────────────────────────
        System.out.println("\n--- ESTADÍSTICAS ---");
        Map<Usuario, int[]> resultados = new HashMap<>();
        resultados.put(alpha,   new int[]{10, 3, 5});  // KDA = 5.0
        resultados.put(bravo,   new int[]{5,  4, 8});  // KDA = 3.25
        resultados.put(charlie, new int[]{8,  2, 6});  // KDA = 7.0 ← MVP
        resultados.put(delta,   new int[]{3,  6, 4});  // KDA = 1.17

        List<Estadistica> stats = facade.registrarEstadisticas(scrim.getId(), resultados);

        System.out.println("\n--- MVP DEL SCRIM ---");
        stats.stream()
             .filter(Estadistica::isMvp)
             .forEach(e -> System.out.println("🏆 MVP: " + e.getUsuario().getUsername() +
                     " | KDA: " + String.format("%.2f", e.getKda())));

        // ── 10. Demostrar error de estado ────────────────────────────
        System.out.println("\n--- DEMO: OPERACIÓN INVÁLIDA EN ESTADO FINALIZADO ---");
        try {
            facade.postular(scrim.getId(), alpha, Rol.DUELIST);
        } catch (IllegalStateException e) {
            System.out.println("[OK] Error esperado: " + e.getMessage());
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("  Flujo completado exitosamente.");
        System.out.println("=".repeat(60));
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
