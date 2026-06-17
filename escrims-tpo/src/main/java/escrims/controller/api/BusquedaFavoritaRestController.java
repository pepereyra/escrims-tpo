package escrims.controller.api;

import escrims.domain.model.AlertaBusqueda;
import escrims.domain.model.BusquedaFavorita;
import escrims.domain.model.Usuario;
import escrims.service.AuthService;
import escrims.service.BusquedaFavoritaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BusquedaFavoritaRestController {

    private final BusquedaFavoritaService service;
    private final AuthService authService;

    public BusquedaFavoritaRestController(BusquedaFavoritaService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @PostMapping("/busquedas-favoritas")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.BusquedaFavoritaResponse guardar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ApiDtos.BusquedaFavoritaRequest request) {
        Usuario usuario = authService.usuarioDesdeAuthorization(authorization);
        return toResponse(service.guardar(
                usuario,
                request.juego(),
                request.formato(),
                request.region(),
                request.rangoMin(),
                request.rangoMax(),
                request.latenciaMax(),
                request.fecha()
        ));
    }

    @GetMapping("/busquedas-favoritas")
    public List<ApiDtos.BusquedaFavoritaResponse> listarBusquedas(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Usuario usuario = authService.usuarioDesdeAuthorization(authorization);
        return service.listarBusquedas(usuario.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/alertas")
    public List<ApiDtos.AlertaBusquedaResponse> listarAlertas(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Usuario usuario = authService.usuarioDesdeAuthorization(authorization);
        return service.listarAlertas(usuario.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private ApiDtos.BusquedaFavoritaResponse toResponse(BusquedaFavorita busqueda) {
        return new ApiDtos.BusquedaFavoritaResponse(
                busqueda.getId(),
                busqueda.getUsuario().getUsername(),
                busqueda.getJuego(),
                busqueda.getFormato(),
                busqueda.getRegion(),
                busqueda.getRangoMin(),
                busqueda.getRangoMax(),
                busqueda.getLatenciaMax(),
                busqueda.getFecha(),
                busqueda.getFechaCreacion()
        );
    }

    private ApiDtos.AlertaBusquedaResponse toResponse(AlertaBusqueda alerta) {
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
