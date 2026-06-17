package escrims.controller.api;

import escrims.domain.model.Usuario;
import escrims.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthService authService;

    public AuthRestController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.AuthResponse register(@RequestBody ApiDtos.RegisterRequest request) {
        Usuario usuario = authService.registrar(
                request.username(),
                request.email(),
                request.password(),
                request.region(),
                request.juego(),
                request.rango(),
                request.latencia(),
                request.rolesPreferidos(),
                request.disponibilidad()
        );

        return new ApiDtos.AuthResponse(authService.login(request.username(), request.password()), toResponse(usuario));
    }

    @PostMapping("/login")
    public ApiDtos.AuthResponse login(@RequestBody ApiDtos.LoginRequest request) {
        String token = authService.login(request.username(), request.password());
        return new ApiDtos.AuthResponse(token, toResponse(authService.usuarioDesdeAuthorization("Bearer " + token)));
    }

    @GetMapping("/me")
    public ApiDtos.UsuarioResponse me(@RequestHeader("Authorization") String authorization) {
        return toResponse(authService.usuarioDesdeAuthorization(authorization));
    }

    @PostMapping("/me/verificar-email")
    public ApiDtos.UsuarioResponse verificarEmail(@RequestHeader("Authorization") String authorization) {
        return toResponse(authService.verificarEmail(authorization));
    }

    @PutMapping("/me/perfil")
    public ApiDtos.UsuarioResponse actualizarPerfil(@RequestHeader("Authorization") String authorization,
                                                    @RequestBody ApiDtos.ActualizarPerfilRequest request) {
        return toResponse(authService.actualizarPerfil(
                authorization,
                request.region(),
                request.juegoPrincipal(),
                request.rango(),
                request.latencia(),
                request.rolesPreferidos(),
                request.disponibilidad(),
                request.rangosPorJuego()
        ));
    }

    private ApiDtos.UsuarioResponse toResponse(Usuario usuario) {
        String juegoPrincipal = resolverJuegoPrincipal(usuario);
        return new ApiDtos.UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRegion(),
                juegoPrincipal,
                usuario.getRangoEnJuego(juegoPrincipal),
                new HashMap<>(usuario.getRangoPorJuego()),
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

    private static String resolverJuegoPrincipal(Usuario usuario) {
        if (usuario.getJuegoPrincipal() != null && !usuario.getJuegoPrincipal().isBlank()) {
            return usuario.getJuegoPrincipal();
        }
        return usuario.getRangoPorJuego().keySet().stream().findFirst().orElse("");
    }
}
