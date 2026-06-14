package escrims;

import escrims.controller.api.ApiDtos;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import escrims.domain.model.Usuario;
import escrims.infra.persistence.SpringDataScrimJpaRepository;
import escrims.infra.persistence.SpringDataUsuarioJpaRepository;
import escrims.infra.persistence.UsuarioJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScrimApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SpringDataUsuarioJpaRepository usuarioJpaRepository;

    @Autowired
    private SpringDataScrimJpaRepository scrimJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Swagger/OpenAPI publica la documentacion de la API REST")
    void swaggerPublicaLaDocumentacionDeLaApiRest() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("eScrims API"))
                .andExpect(jsonPath("$.paths./api/auth/register").exists())
                .andExpect(jsonPath("$.paths./api/usuarios").exists())
                .andExpect(jsonPath("$.paths./api/scrims").exists());

        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("API REST permite registro, login JWT y edicion de perfil")
    void apiRestPermiteRegistroLoginJwtYPerfilEditable() throws Exception {
        String registerResponse = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.RegisterRequest(
                                "AuthAlpha",
                                "auth-alpha@mail.com",
                                "secreto123",
                                "SA",
                                "Valorant",
                                1500,
                                35,
                                List.of("DUELIST", "SUPPORT"),
                                "Lunes 20-23"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.username").value("AuthAlpha"))
                .andExpect(jsonPath("$.usuario.verificado").value(false))
                .andExpect(jsonPath("$.usuario.rolesPreferidos").value(hasItem("DUELIST")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String registerToken = read(registerResponse).get("token").asText();
        String storedPassword = usuarioJpaRepository.findByUsername("AuthAlpha").orElseThrow().toDomain().getPasswordHash();
        assertTrue(storedPassword.startsWith("pbkdf2$"));
        assertTrue(!storedPassword.equals("secreto123"));

        String loginResponse = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.LoginRequest("AuthAlpha", "secreto123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String loginToken = read(loginResponse).get("token").asText();

        mvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("AuthAlpha"));

        mvc.perform(post("/api/auth/me/verificar-email")
                        .header("Authorization", "Bearer " + registerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificado").value(true));

        mvc.perform(put("/api/auth/me/perfil")
                        .header("Authorization", "Bearer " + loginToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.ActualizarPerfilRequest(
                                "BR",
                                "LoL",
                                1700,
                                45,
                                List.of("JUNGLA", "MID"),
                                "Martes y jueves"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region").value("BR"))
                .andExpect(jsonPath("$.juegoPrincipal").value("LoL"))
                .andExpect(jsonPath("$.rangoPrincipal").value(1700))
                .andExpect(jsonPath("$.rolesPreferidos").value(hasItem("JUNGLA")))
                .andExpect(jsonPath("$.disponibilidad").value("Martes y jueves"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.LoginRequest("AuthAlpha", "mal-password"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Credenciales invalidas."));
    }

    @Test
    @DisplayName("API REST expone el flujo completo de scrim")
    void apiRestExponeFlujoCompletoDeScrim() throws Exception {
        LocalDateTime fechaHora = LocalDateTime.now().plusHours(2).withNano(0);

        crearUsuario("ApiAlpha", "api-alpha@mail.com", 1500, 30);
        crearUsuario("ApiBravo", "api-bravo@mail.com", 1600, 25);
        crearUsuario("ApiCharlie", "api-charlie@mail.com", 1450, 40);
        crearUsuario("ApiDelta", "api-delta@mail.com", 1550, 35);

        String crearScrimResponse = mvc.perform(post("/api/scrims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.CrearScrimRequest(
                                "Valorant",
                                "2v2",
                                "SA",
                                1400,
                                1700,
                                80,
                                fechaHora,
                                30,
                                4
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BUSCANDO"))
                .andExpect(jsonPath("$.cuposDisponibles").value(4))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID scrimId = UUID.fromString(read(crearScrimResponse).get("id").asText());

        mvc.perform(post("/api/notificaciones/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.NotificacionesRequest(List.of(
                                "ApiAlpha",
                                "ApiBravo",
                                "ApiCharlie",
                                "ApiDelta"
                        )))))
                .andExpect(status().isNoContent());

        postular(scrimId, "ApiAlpha", "DUELIST", "BUSCANDO");
        postular(scrimId, "ApiBravo", "SUPPORT", "BUSCANDO");
        postular(scrimId, "ApiCharlie", "DUELIST", "BUSCANDO");
        postular(scrimId, "ApiDelta", "SUPPORT", "LOBBY_ARMADO");

        mvc.perform(post("/api/scrims/{scrimId}/roles/intercambiar", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.IntercambiarRolesRequest("ApiAlpha", "ApiBravo"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postulaciones[?(@.username == 'ApiAlpha')].rol").value(hasItem("SUPPORT")))
                .andExpect(jsonPath("$.postulaciones[?(@.username == 'ApiBravo')].rol").value(hasItem("DUELIST")));

        confirmar(scrimId, "ApiAlpha", "LOBBY_ARMADO");
        confirmar(scrimId, "ApiBravo", "LOBBY_ARMADO");
        confirmar(scrimId, "ApiCharlie", "LOBBY_ARMADO");
        confirmar(scrimId, "ApiDelta", "CONFIRMADO");

        mvc.perform(post("/api/scrims/scheduler")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.SchedulerRequest(fechaHora.plusSeconds(1)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iniciados").value(1));

        mvc.perform(get("/api/scrims/{scrimId}", scrimId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_JUEGO"));

        mvc.perform(post("/api/scrims/{scrimId}/finalizar", scrimId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));

        mvc.perform(post("/api/scrims/{scrimId}/estadisticas", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.RegistrarEstadisticasRequest(List.of(
                                new ApiDtos.EstadisticaInput("ApiAlpha", 10, 3, 5),
                                new ApiDtos.EstadisticaInput("ApiBravo", 5, 4, 8),
                                new ApiDtos.EstadisticaInput("ApiCharlie", 8, 2, 6),
                                new ApiDtos.EstadisticaInput("ApiDelta", 3, 6, 4)
                        )))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.mvp == true)].username").value(hasItem("ApiCharlie")));

        assertTrue(usuarioJpaRepository.findByUsername("ApiAlpha").isPresent());
        assertEquals("FINALIZADO", scrimJpaRepository.findById(scrimId).orElseThrow().getEstado());
        assertEquals(4, countRows("postulaciones", scrimId));
        assertEquals(4, countRows("confirmaciones", scrimId));
        assertEquals(4, countRows("estadisticas", scrimId));
    }

    @Test
    @DisplayName("API REST permite buscar scrims por filtros")
    void apiRestPermiteBuscarScrimsPorFiltros() throws Exception {
        LocalDateTime fechaHora = LocalDateTime.now().plusHours(3).withNano(0);

        mvc.perform(post("/api/scrims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.CrearScrimRequest(
                                "SearchGame",
                                "3v3",
                                "BR",
                                1200,
                                1500,
                                60,
                                fechaHora,
                                45,
                                6
                        ))))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/scrims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.CrearScrimRequest(
                                "SearchGameOther",
                                "5v5",
                                "NA",
                                1800,
                                2200,
                                90,
                                fechaHora.plusDays(1),
                                60,
                                10
                        ))))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/scrims")
                        .param("juego", "SearchGame")
                        .param("formato", "3v3")
                        .param("region", "BR")
                        .param("rangoMin", "1200")
                        .param("rangoMax", "1500")
                        .param("latenciaMax", "60")
                        .param("fecha", fechaHora.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].juego").value("SearchGame"))
                .andExpect(jsonPath("$[0].region").value("BR"));
    }

    @Test
    @DisplayName("API REST permite feedback y moderacion de reportes")
    void apiRestPermiteFeedbackYModeracionDeReportes() throws Exception {
        LocalDateTime fechaHora = LocalDateTime.now().plusHours(2).withNano(0);

        crearUsuario("ApiFeedAlpha", "api-feed-alpha@mail.com", 1500, 30);
        crearUsuario("ApiFeedBravo", "api-feed-bravo@mail.com", 1600, 25);
        crearUsuario("ApiFeedCharlie", "api-feed-charlie@mail.com", 1450, 40);
        crearUsuario("ApiFeedDelta", "api-feed-delta@mail.com", 1550, 35);

        UUID scrimId = crearScrim(fechaHora);

        postular(scrimId, "ApiFeedAlpha", "DUELIST", "BUSCANDO");
        postular(scrimId, "ApiFeedBravo", "SUPPORT", "BUSCANDO");
        postular(scrimId, "ApiFeedCharlie", "DUELIST", "BUSCANDO");
        postular(scrimId, "ApiFeedDelta", "SUPPORT", "LOBBY_ARMADO");

        confirmar(scrimId, "ApiFeedAlpha", "LOBBY_ARMADO");
        confirmar(scrimId, "ApiFeedBravo", "LOBBY_ARMADO");
        confirmar(scrimId, "ApiFeedCharlie", "LOBBY_ARMADO");
        confirmar(scrimId, "ApiFeedDelta", "CONFIRMADO");

        mvc.perform(post("/api/scrims/{scrimId}/iniciar", scrimId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_JUEGO"));

        mvc.perform(post("/api/scrims/{scrimId}/finalizar", scrimId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));

        String feedbackResponse = mvc.perform(post("/api/scrims/{scrimId}/feedback", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.FeedbackRequest(
                                "ApiFeedAlpha",
                                "ApiFeedBravo",
                                5,
                                "Buen compañero"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID feedbackId = UUID.fromString(read(feedbackResponse).get("id").asText());
        String userToken = registrarUsuarioAuth("ApiFeedUser", "api-feed-user@mail.com");
        String modToken = registrarModerador("ApiFeedMod", "api-feed-mod@mail.com");

        mvc.perform(post("/api/feedback/{feedbackId}/aprobar", feedbackId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authorization Bearer token requerido."));

        mvc.perform(post("/api/feedback/{feedbackId}/aprobar", feedbackId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/feedback/{feedbackId}/aprobar", feedbackId)
                        .header("Authorization", "Bearer " + modToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"));

        String reporteResponse = mvc.perform(post("/api/scrims/{scrimId}/reportes", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.ReporteConductaRequest(
                                "ApiFeedAlpha",
                                "ApiFeedBravo",
                                "No-show parcial"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID reporteId = UUID.fromString(read(reporteResponse).get("id").asText());

        mvc.perform(post("/api/reportes/{reporteId}/aprobar", reporteId)
                        .header("Authorization", "Bearer " + modToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.ResolverReporteRequest("STRIKE_NO_SHOW"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.sancion").value("STRIKE_NO_SHOW"))
                .andExpect(jsonPath("$.strikesReportado").value(1));

        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'ApiFeedBravo')].strikes").value(hasItem(1)));

        assertEquals(1, countRows("feedback", scrimId));
        assertEquals(1, countRows("reportes_conducta", scrimId));
    }

    @Test
    @DisplayName("API REST devuelve 400 cuando el usuario no existe")
    void apiRestDevuelveBadRequestCuandoUsuarioNoExiste() throws Exception {
        LocalDateTime fechaHora = LocalDateTime.now().plusHours(2).withNano(0);
        UUID scrimId = crearScrim(fechaHora);

        mvc.perform(post("/api/scrims/{scrimId}/postulaciones", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.PostulacionRequest("UsuarioInexistente", "DUELIST"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado: UsuarioInexistente"));
    }

    @Test
    @DisplayName("API REST permite mover un jugador aceptado a suplente")
    void apiRestPermiteMoverJugadorASuplente() throws Exception {
        LocalDateTime fechaHora = LocalDateTime.now().plusHours(2).withNano(0);

        crearUsuario("ApiRoleAlpha", "api-role-alpha@mail.com", 1500, 30);
        crearUsuario("ApiRoleBravo", "api-role-bravo@mail.com", 1600, 25);
        crearUsuario("ApiRoleCharlie", "api-role-charlie@mail.com", 1450, 40);
        crearUsuario("ApiRoleDelta", "api-role-delta@mail.com", 1550, 35);

        UUID scrimId = crearScrim(fechaHora);

        postular(scrimId, "ApiRoleAlpha", "DUELIST", "BUSCANDO");
        postular(scrimId, "ApiRoleBravo", "SUPPORT", "BUSCANDO");
        postular(scrimId, "ApiRoleCharlie", "DUELIST", "BUSCANDO");
        postular(scrimId, "ApiRoleDelta", "SUPPORT", "LOBBY_ARMADO");

        mvc.perform(post("/api/scrims/{scrimId}/suplentes", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.UsuarioOperacionRequest("ApiRoleDelta"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BUSCANDO"))
                .andExpect(jsonPath("$.cuposDisponibles").value(1))
                .andExpect(jsonPath("$.postulaciones[?(@.username == 'ApiRoleDelta')].estado").value(hasItem("SUPLENTE")));

        assertEquals(4, countRows("postulaciones", scrimId));
        assertEquals(3, countRows("confirmaciones", scrimId));
    }

    @Test
    @DisplayName("API REST devuelve 409 cuando la transicion de estado no es valida")
    void apiRestDevuelveConflictCuandoLaTransicionNoEsValida() throws Exception {
        LocalDateTime fechaHora = LocalDateTime.now().plusHours(2).withNano(0);
        UUID scrimId = crearScrim(fechaHora);

        crearUsuario("ApiEcho", "api-echo@mail.com", 1500, 30);

        mvc.perform(post("/api/scrims/{scrimId}/confirmaciones", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.UsuarioOperacionRequest("ApiEcho"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("No se puede confirmar: el scrim aún está buscando jugadores."));
    }

    private void crearUsuario(String username, String email, int rango, int latencia) throws Exception {
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.CrearUsuarioRequest(
                                username,
                                email,
                                "hash123",
                                "SA",
                                "Valorant",
                                rango,
                                latencia,
                                true
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.verificado").value(true));
    }

    private UUID crearScrim(LocalDateTime fechaHora) throws Exception {
        String response = mvc.perform(post("/api/scrims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.CrearScrimRequest(
                                "Valorant",
                                "2v2",
                                "SA",
                                1400,
                                1700,
                                80,
                                fechaHora,
                                30,
                                4
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(read(response).get("id").asText());
    }

    private void postular(UUID scrimId, String username, String rol, String estadoEsperado) throws Exception {
        mvc.perform(post("/api/scrims/{scrimId}/postulaciones", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.PostulacionRequest(username, rol))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(estadoEsperado));
    }

    private void confirmar(UUID scrimId, String username, String estadoEsperado) throws Exception {
        mvc.perform(post("/api/scrims/{scrimId}/confirmaciones", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.UsuarioOperacionRequest(username))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(estadoEsperado));
    }

    private String registrarUsuarioAuth(String username, String email) throws Exception {
        String response = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.RegisterRequest(
                                username,
                                email,
                                "secreto123",
                                "SA",
                                "Valorant",
                                1500,
                                35,
                                List.of("DUELIST"),
                                "Disponible"
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return read(response).get("token").asText();
    }

    private String registrarModerador(String username, String email) throws Exception {
        registrarUsuarioAuth(username, email);

        Usuario usuario = usuarioJpaRepository.findByUsername(username).orElseThrow().toDomain();
        usuario.setRolSistema("MOD");
        usuarioJpaRepository.save(UsuarioJpaEntity.fromDomain(usuario));

        String response = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ApiDtos.LoginRequest(username, "secreto123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return read(response).get("token").asText();
    }

    private String json(Object body) throws Exception {
        return mapper.writeValueAsString(body);
    }

    private JsonNode read(String json) throws Exception {
        return mapper.readTree(json);
    }

    private int countRows(String table, UUID scrimId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from " + table + " where scrim_id = ?",
                Integer.class,
                scrimId
        );
        return count == null ? 0 : count;
    }
}
