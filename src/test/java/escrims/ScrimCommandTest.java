package escrims;

import escrims.controller.ScrimController;
import escrims.domain.model.Postulacion;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;
import escrims.infra.events.DomainEventBus;
import escrims.infra.notification.DevNotificadorFactory;
import escrims.service.ScrimService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScrimCommandTest {

    private ScrimController controller;
    private Usuario alpha;
    private Usuario bravo;
    private Usuario charlie;
    private Usuario delta;

    @BeforeEach
    void setUp() {
        controller = new ScrimController(new ScrimService(
                new DomainEventBus(),
                new DevNotificadorFactory()
        ));

        alpha = crearUsuario("Alpha", "alpha@mail.com", 1500, 30);
        bravo = crearUsuario("Bravo", "bravo@mail.com", 1600, 25);
        charlie = crearUsuario("Charlie", "charlie@mail.com", 1450, 40);
        delta = crearUsuario("Delta", "delta@mail.com", 1550, 35);
    }

    @Test
    @DisplayName("Command permite cambiar, intercambiar roles y mover un jugador a suplente")
    void commandGestionaRolesYSuplentes() {
        ScrimContext scrim = crearScrim2v2();
        postularTodos(scrim);

        controller.cambiarRol(scrim.getId(), alpha, Rol.MID);
        assertEquals("MID", postulacionDe(scrim, alpha).getRolDeseado().getNombre());

        controller.intercambiarRoles(scrim.getId(), alpha, bravo);
        assertEquals("SUPPORT", postulacionDe(scrim, alpha).getRolDeseado().getNombre());
        assertEquals("MID", postulacionDe(scrim, bravo).getRolDeseado().getNombre());

        controller.moverASuplente(scrim.getId(), delta);

        assertEquals("SUPLENTE", postulacionDe(scrim, delta).getEstado().getNombre());
        assertEquals(1, scrim.cuposDisponibles());
        assertEquals("BUSCANDO", scrim.getState().getNombre());

        controller.reactivarTitular(scrim.getId(), delta);

        assertEquals("ACEPTADA", postulacionDe(scrim, delta).getEstado().getNombre());
        assertEquals(0, scrim.cuposDisponibles());
        assertEquals("LOBBY_ARMADO", scrim.getState().getNombre());
    }

    @Test
    @DisplayName("Command no permite gestionar roles cuando el scrim ya esta en juego")
    void commandNoGestionaRolesEnJuego() {
        ScrimContext scrim = crearScrim2v2();
        postularTodos(scrim);
        confirmarTodos(scrim);
        controller.iniciar(scrim.getId());

        assertThrows(IllegalStateException.class,
                () -> controller.cambiarRol(scrim.getId(), alpha, Rol.MID));
    }

    private ScrimContext crearScrim2v2() {
        return controller.crearScrim(
                "Valorant",
                "2v2",
                "SA",
                1400,
                1700,
                80,
                LocalDateTime.now().plusHours(2),
                30,
                4
        );
    }

    private void postularTodos(ScrimContext scrim) {
        controller.postular(scrim.getId(), alpha, Rol.DUELIST);
        controller.postular(scrim.getId(), bravo, Rol.SUPPORT);
        controller.postular(scrim.getId(), charlie, Rol.DUELIST);
        controller.postular(scrim.getId(), delta, Rol.SUPPORT);
    }

    private void confirmarTodos(ScrimContext scrim) {
        controller.confirmar(scrim.getId(), alpha);
        controller.confirmar(scrim.getId(), bravo);
        controller.confirmar(scrim.getId(), charlie);
        controller.confirmar(scrim.getId(), delta);
    }

    private Postulacion postulacionDe(ScrimContext scrim, Usuario usuario) {
        return scrim.getPostulaciones().stream()
                .filter(postulacion -> postulacion.getUsuario().getId().equals(usuario.getId()))
                .findFirst()
                .orElseThrow();
    }

    private static Usuario crearUsuario(String username, String email, int rango, int latencia) {
        Usuario usuario = new Usuario(username, email, "hash123", "SA");
        usuario.verificarEmail();
        usuario.setLatenciaPromedio(latencia);

        Map<String, Integer> rangos = new HashMap<>();
        rangos.put("Valorant", rango);
        usuario.setRangoPorJuego(rangos);

        return usuario;
    }
}
