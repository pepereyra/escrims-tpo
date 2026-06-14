package escrims;

import escrims.controller.api.ApiDtos;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScrimApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @DisplayName("Swagger/OpenAPI publica la documentacion de la API REST")
    void swaggerPublicaLaDocumentacionDeLaApiRest() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("eScrims API"))
                .andExpect(jsonPath("$.paths./api/usuarios").exists())
                .andExpect(jsonPath("$.paths./api/scrims").exists());

        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
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

    private String json(Object body) throws Exception {
        return mapper.writeValueAsString(body);
    }

    private JsonNode read(String json) throws Exception {
        return mapper.readTree(json);
    }
}
