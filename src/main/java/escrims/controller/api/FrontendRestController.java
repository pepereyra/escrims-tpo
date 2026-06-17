package escrims.controller.api;

import escrims.domain.model.AlertaBusqueda;
import escrims.domain.model.Confirmacion;
import escrims.domain.model.Equipo;
import escrims.domain.model.Postulacion;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;
import escrims.facade.ScrimFacade;
import escrims.service.AuthService;
import escrims.service.BusquedaFavoritaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FrontendRestController {

    private final ScrimFacade facade;
    private final AuthService authService;
    private final BusquedaFavoritaService busquedaFavoritaService;

    public FrontendRestController(ScrimFacade facade,
                                  AuthService authService,
                                  BusquedaFavoritaService busquedaFavoritaService) {
        this.facade = facade;
        this.authService = authService;
        this.busquedaFavoritaService = busquedaFavoritaService;
    }

    @GetMapping("/dashboard/me")
    public ApiDtos.DashboardMeResponse dashboard(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Usuario usuario = authService.usuarioDesdeAuthorization(authorization);
        List<ApiDtos.MiScrimResponse> misScrims = misScrimsDe(usuario);
        List<AlertaBusqueda> alertas = busquedaFavoritaService.listarAlertas(usuario.getId()).stream()
                .toList();

        return new ApiDtos.DashboardMeResponse(
                toUsuarioResponse(usuario),
                misScrims.size(),
                (int) misScrims.stream().filter(ApiDtos.MiScrimResponse::puedeConfirmar).count(),
                alertas.size(),
                busquedaFavoritaService.listarBusquedas(usuario.getId()).size(),
                misScrims.stream()
                        .filter(scrim -> !scrim.estado().equals("FINALIZADO"))
                        .filter(scrim -> !scrim.estado().equals("CANCELADO"))
                        .filter(scrim -> scrim.fechaHora().isAfter(LocalDateTime.now().minusMinutes(1)))
                        .sorted(Comparator.comparing(ApiDtos.MiScrimResponse::fechaHora))
                        .limit(5)
                        .toList(),
                alertas.stream()
                        .sorted(Comparator.comparing(AlertaBusqueda::getFechaCreacion).reversed())
                        .limit(5)
                        .map(this::toAlertaResponse)
                        .toList()
        );
    }

    @GetMapping("/scrims/mis-scrims")
    public ApiDtos.MisScrimsResponse misScrims(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Usuario usuario = authService.usuarioDesdeAuthorization(authorization);
        return new ApiDtos.MisScrimsResponse(misScrimsDe(usuario));
    }

    @GetMapping("/scrims/{scrimId}/participantes")
    public ApiDtos.ParticipantesScrimResponse participantes(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("scrimId") UUID scrimId) {
        authService.usuarioDesdeAuthorization(authorization);
        ScrimContext scrim = facade.getScrim(scrimId);

        return new ApiDtos.ParticipantesScrimResponse(
                scrim.getId(),
                scrim.getState().getNombre(),
                scrim.getCuposTotales(),
                scrim.cuposDisponibles(),
                scrim.getEquipos().stream()
                        .map(equipo -> toEquipoResponse(scrim, equipo))
                        .toList(),
                participantesPorEstado(scrim, "ACEPTADA"),
                participantesPorEstado(scrim, "SUPLENTE"),
                participantesPorEstado(scrim, "PENDIENTE"),
                participantesPorEstado(scrim, "RECHAZADA")
        );
    }

    private List<ApiDtos.MiScrimResponse> misScrimsDe(Usuario usuario) {
        return facade.listarScrims().stream()
                .filter(scrim -> postulacionDe(scrim, usuario) != null)
                .map(scrim -> toMiScrimResponse(scrim, usuario))
                .sorted(Comparator.comparing(ApiDtos.MiScrimResponse::fechaHora))
                .toList();
    }

    private ApiDtos.MiScrimResponse toMiScrimResponse(ScrimContext scrim, Usuario usuario) {
        Postulacion postulacion = postulacionDe(scrim, usuario);
        Confirmacion confirmacion = confirmacionDe(scrim, usuario);
        boolean confirmado = confirmacion != null && confirmacion.isConfirmado();

        return new ApiDtos.MiScrimResponse(
                scrim.getId(),
                scrim.getJuego(),
                scrim.getFormato(),
                scrim.getRegion(),
                scrim.getState().getNombre(),
                scrim.getModalidad(),
                scrim.getCuposTotales(),
                scrim.cuposDisponibles(),
                scrim.getFechaHora(),
                postulacion == null ? "" : postulacion.getRolDeseado().getNombre(),
                postulacion == null ? "" : postulacion.getEstado().getNombre(),
                confirmado,
                "LOBBY_ARMADO".equals(scrim.getState().getNombre()) && !confirmado
        );
    }

    private List<ApiDtos.ParticipanteScrimResponse> participantesPorEstado(ScrimContext scrim, String estado) {
        return scrim.getPostulaciones().stream()
                .filter(postulacion -> estado.equals(postulacion.getEstado().getNombre()))
                .map(postulacion -> toParticipanteResponse(scrim, postulacion))
                .toList();
    }

    private ApiDtos.ParticipanteScrimResponse toParticipanteResponse(ScrimContext scrim,
                                                                     Postulacion postulacion) {
        Usuario usuario = postulacion.getUsuario();
        Confirmacion confirmacion = confirmacionDe(scrim, usuario);
        return new ApiDtos.ParticipanteScrimResponse(
                usuario.getUsername(),
                postulacion.getRolDeseado().getNombre(),
                postulacion.getEstado().getNombre(),
                confirmacion != null && confirmacion.isConfirmado(),
                confirmacion == null ? null : confirmacion.getFechaConfirmacion(),
                usuario.getRangoEnJuego(scrim.getJuego()),
                usuario.getLatenciaPromedio()
        );
    }

    private ApiDtos.EquipoResponse toEquipoResponse(ScrimContext scrim, Equipo equipo) {
        return new ApiDtos.EquipoResponse(
                equipo.getLado(),
                equipo.getJugadores().stream()
                        .map(usuario -> {
                            Postulacion postulacion = postulacionDe(scrim, usuario);
                            return toParticipanteResponse(scrim, postulacion);
                        })
                        .toList()
        );
    }

    private Postulacion postulacionDe(ScrimContext scrim, Usuario usuario) {
        return scrim.getPostulaciones().stream()
                .filter(postulacion -> postulacion.getUsuario().getId().equals(usuario.getId()))
                .findFirst()
                .orElse(null);
    }

    private Confirmacion confirmacionDe(ScrimContext scrim, Usuario usuario) {
        return scrim.getConfirmaciones().stream()
                .filter(confirmacion -> confirmacion.getUsuario().getId().equals(usuario.getId()))
                .findFirst()
                .orElse(null);
    }

    private ApiDtos.UsuarioResponse toUsuarioResponse(Usuario usuario) {
        return new ApiDtos.UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRegion(),
                usuario.getRangoPorJuego().keySet().stream().findFirst().orElse(""),
                usuario.getRangoPorJuego().values().stream().findFirst().orElse(0),
                usuario.getRangoPorJuego(),
                usuario.getLatenciaPromedio(),
                usuario.getRolesPreferidos().stream()
                        .map(rol -> rol.getNombre())
                        .toList(),
                usuario.getDisponibilidad(),
                usuario.isVerificado(),
                usuario.getRolSistema(),
                usuario.getStrikes(),
                usuario.getCooldownHasta()
        );
    }

    private ApiDtos.AlertaBusquedaResponse toAlertaResponse(AlertaBusqueda alerta) {
        return new ApiDtos.AlertaBusquedaResponse(
                alerta.getId(),
                alerta.getBusquedaId(),
                alerta.getUsuario().getUsername(),
                alerta.getScrimId(),
                alerta.getMensaje(),
                alerta.getFechaCreacion()
        );
    }
}
