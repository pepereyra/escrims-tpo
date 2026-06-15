package escrims.controller.api;

import escrims.domain.model.Estadistica;
import escrims.domain.model.RecordatorioScrim;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;
import escrims.facade.ScrimFacade;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ScrimRestController {

    private final ScrimFacade facade;
    private final UsuarioApiRepository usuarios;

    public ScrimRestController(ScrimFacade facade, UsuarioApiRepository usuarios) {
        this.facade = facade;
        this.usuarios = usuarios;
    }

    @PostMapping("/scrims")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.ScrimResponse crearScrim(@RequestBody ApiDtos.CrearScrimRequest request) {
        ScrimContext scrim = facade.crearScrim(
                request.juego(),
                request.formato(),
                request.region(),
                request.rangoMin(),
                request.rangoMax(),
                request.latenciaMax(),
                request.fechaHora(),
                request.duracionMinutos(),
                request.cuposTotales(),
                request.modalidad()
        );

        return toResponse(scrim);
    }

    @GetMapping("/scrims/{scrimId}")
    public ApiDtos.ScrimResponse obtenerScrim(@PathVariable("scrimId") UUID scrimId) {
        return toResponse(facade.getScrim(scrimId));
    }

    @GetMapping("/scrims")
    public List<ApiDtos.ScrimResponse> buscarScrims(@RequestParam(value = "juego", required = false) String juego,
                                                    @RequestParam(value = "formato", required = false) String formato,
                                                    @RequestParam(value = "region", required = false) String region,
                                                    @RequestParam(value = "rangoMin", required = false) Integer rangoMin,
                                                    @RequestParam(value = "rangoMax", required = false) Integer rangoMax,
                                                    @RequestParam(value = "fecha", required = false) LocalDateTime fecha,
                                                    @RequestParam(value = "latenciaMax", required = false) Integer latenciaMax) {
        return facade.listarScrims().stream()
                .filter(scrim -> coincide(juego, scrim.getJuego()))
                .filter(scrim -> coincide(formato, scrim.getFormato()))
                .filter(scrim -> coincide(region, scrim.getRegion()))
                .filter(scrim -> rangoMin == null || scrim.getRangoMin() >= rangoMin)
                .filter(scrim -> rangoMax == null || scrim.getRangoMax() <= rangoMax)
                .filter(scrim -> fecha == null || scrim.getFechaHora().toLocalDate().equals(fecha.toLocalDate()))
                .filter(scrim -> latenciaMax == null || scrim.getLatenciaMax() <= latenciaMax)
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/scrims/{scrimId}/postulaciones")
    public ApiDtos.ScrimResponse postular(@PathVariable("scrimId") UUID scrimId,
                                           @RequestBody ApiDtos.PostulacionRequest request) {
        facade.postular(scrimId, usuarios.buscar(request.username()), new Rol(request.rol()));
        return toResponse(facade.getScrim(scrimId));
    }

    @PostMapping("/scrims/{scrimId}/confirmaciones")
    public ApiDtos.ScrimResponse confirmar(@PathVariable("scrimId") UUID scrimId,
                                            @RequestBody ApiDtos.UsuarioOperacionRequest request) {
        facade.confirmar(scrimId, usuarios.buscar(request.username()));
        return toResponse(facade.getScrim(scrimId));
    }

    @PostMapping("/scrims/{scrimId}/iniciar")
    public ApiDtos.ScrimResponse iniciar(@PathVariable("scrimId") UUID scrimId) {
        facade.iniciar(scrimId);
        return toResponse(facade.getScrim(scrimId));
    }

    @PostMapping("/scrims/scheduler")
    public ApiDtos.SchedulerResponse procesarScheduler(@RequestBody(required = false) ApiDtos.SchedulerRequest request) {
        LocalDateTime ahora = request == null || request.ahora() == null
                ? LocalDateTime.now()
                : request.ahora();

        return new ApiDtos.SchedulerResponse(facade.procesarScrimsProgramados(ahora));
    }

    @GetMapping("/scrims/{scrimId}/ical")
    public ApiDtos.IcalResponse generarIcal(@PathVariable("scrimId") UUID scrimId) {
        return new ApiDtos.IcalResponse(scrimId, facade.generarIcal(scrimId));
    }

    @PostMapping("/scrims/recordatorios")
    public List<ApiDtos.RecordatorioResponse> procesarRecordatorios(
            @RequestBody ApiDtos.RecordatoriosRequest request) {
        LocalDateTime ahora = request == null || request.ahora() == null
                ? LocalDateTime.now()
                : request.ahora();
        int horasAntes = request == null || request.horasAntes() <= 0 ? 1 : request.horasAntes();

        return facade.procesarRecordatorios(ahora, horasAntes).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/scrims/{scrimId}/finalizar")
    public ApiDtos.ScrimResponse finalizar(@PathVariable("scrimId") UUID scrimId) {
        facade.finalizar(scrimId);
        return toResponse(facade.getScrim(scrimId));
    }

    @PostMapping("/scrims/{scrimId}/cancelar")
    public ApiDtos.ScrimResponse cancelar(@PathVariable("scrimId") UUID scrimId) {
        facade.cancelar(scrimId);
        return toResponse(facade.getScrim(scrimId));
    }

    @PostMapping("/scrims/{scrimId}/roles/cambiar")
    public ApiDtos.ScrimResponse cambiarRol(@PathVariable("scrimId") UUID scrimId,
                                             @RequestBody ApiDtos.CambiarRolRequest request) {
        facade.cambiarRol(scrimId, usuarios.buscar(request.username()), new Rol(request.nuevoRol()));
        return toResponse(facade.getScrim(scrimId));
    }

    @PostMapping("/scrims/{scrimId}/roles/intercambiar")
    public ApiDtos.ScrimResponse intercambiarRoles(@PathVariable("scrimId") UUID scrimId,
                                                   @RequestBody ApiDtos.IntercambiarRolesRequest request) {
        facade.intercambiarRoles(
                scrimId,
                usuarios.buscar(request.usernameA()),
                usuarios.buscar(request.usernameB())
        );
        return toResponse(facade.getScrim(scrimId));
    }

    @PostMapping("/scrims/{scrimId}/suplentes")
    public ApiDtos.ScrimResponse moverASuplente(@PathVariable("scrimId") UUID scrimId,
                                                @RequestBody ApiDtos.UsuarioOperacionRequest request) {
        facade.moverASuplente(scrimId, usuarios.buscar(request.username()));
        return toResponse(facade.getScrim(scrimId));
    }

    @PostMapping("/scrims/{scrimId}/comandos/undo")
    public ApiDtos.ScrimResponse deshacerUltimoComando(@PathVariable("scrimId") UUID scrimId) {
        facade.deshacerUltimoComando(scrimId);
        return toResponse(facade.getScrim(scrimId));
    }

    @PostMapping("/scrims/{scrimId}/estadisticas")
    public List<ApiDtos.EstadisticaResponse> registrarEstadisticas(
            @PathVariable("scrimId") UUID scrimId,
            @RequestBody ApiDtos.RegistrarEstadisticasRequest request) {

        Map<Usuario, int[]> resultados = new LinkedHashMap<>();

        for (ApiDtos.EstadisticaInput input : request.resultados()) {
            resultados.put(
                    usuarios.buscar(input.username()),
                    new int[] {input.kills(), input.deaths(), input.assists()}
            );
        }

        return facade.registrarEstadisticas(scrimId, resultados).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/notificaciones/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void configurarEmail(@RequestBody ApiDtos.NotificacionesRequest request) {
        facade.configurarNotificacionesEmail(buscarUsuarios(request));
    }

    @PostMapping("/notificaciones/push")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void configurarPush(@RequestBody ApiDtos.NotificacionesRequest request) {
        facade.configurarNotificacionesPush(buscarUsuarios(request));
    }

    @PostMapping("/notificaciones/discord")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void configurarDiscord(@RequestBody ApiDtos.NotificacionesRequest request) {
        facade.configurarNotificacionesDiscord(buscarUsuarios(request));
    }

    private List<Usuario> buscarUsuarios(ApiDtos.NotificacionesRequest request) {
        return request.usernames().stream()
                .map(usuarios::buscar)
                .toList();
    }

    private boolean coincide(String filtro, String valor) {
        return filtro == null || filtro.isBlank() || valor.equalsIgnoreCase(filtro);
    }

    private ApiDtos.ScrimResponse toResponse(ScrimContext scrim) {
        return new ApiDtos.ScrimResponse(
                scrim.getId(),
                scrim.getJuego(),
                scrim.getFormato(),
                scrim.getRegion(),
                scrim.getState().getNombre(),
                scrim.getCuposTotales(),
                scrim.getModalidad(),
                scrim.cuposDisponibles(),
                scrim.getFechaHora(),
                scrim.getPostulaciones().stream()
                        .map(postulacion -> new ApiDtos.PostulacionResponse(
                                postulacion.getUsuario().getUsername(),
                                postulacion.getRolDeseado().getNombre(),
                                postulacion.getEstado().getNombre()
                        ))
                        .toList()
        );
    }

    private ApiDtos.EstadisticaResponse toResponse(Estadistica estadistica) {
        return new ApiDtos.EstadisticaResponse(
                estadistica.getUsuario().getUsername(),
                estadistica.getKills(),
                estadistica.getDeaths(),
                estadistica.getAssists(),
                estadistica.getKda(),
                estadistica.isMvp()
        );
    }

    private ApiDtos.RecordatorioResponse toResponse(RecordatorioScrim recordatorio) {
        return new ApiDtos.RecordatorioResponse(
                recordatorio.getScrimId(),
                recordatorio.getUsername(),
                recordatorio.getHorasAntes(),
                recordatorio.getCanal(),
                recordatorio.getIcal(),
                recordatorio.getFechaEnvio()
        );
    }
}
