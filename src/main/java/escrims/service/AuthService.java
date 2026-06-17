package escrims.service;

import escrims.controller.api.ForbiddenException;
import escrims.controller.api.UnauthorizedException;
import escrims.controller.api.UsuarioApiRepository;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthService {

    private final UsuarioApiRepository usuarios;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;

    public AuthService(UsuarioApiRepository usuarios,
                       PasswordHasher passwordHasher,
                       JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
    }

    public Usuario registrar(String username,
                             String email,
                             String password,
                             String region,
                             String juego,
                             int rango,
                             int latencia,
                             List<String> rolesPreferidos,
                             String disponibilidad) {
        if (usuarios.existeUsername(username)) {
            throw new IllegalArgumentException("El username ya esta registrado.");
        }
        if (usuarios.existeEmail(email)) {
            throw new IllegalArgumentException("El email ya esta registrado.");
        }

        Usuario usuario = new Usuario(username, email, passwordHasher.hash(password), region);
        usuario.setLatenciaPromedio(latencia);
        usuario.setDisponibilidad(disponibilidad);
        usuario.setRolSistema("USER");
        usuario.setRolesPreferidos(toRoles(rolesPreferidos));

        Map<String, Integer> rangos = new HashMap<>();
        rangos.put(juego, rango);
        usuario.setRangoPorJuego(rangos);

        return usuarios.guardar(usuario);
    }

    public String login(String username, String password) {
        Usuario usuario = usuarios.buscar(username);
        if (!passwordHasher.verificar(password, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales invalidas.");
        }

        return jwtService.emitirToken(usuario);
    }

    public Usuario usuarioDesdeAuthorization(String authorizationHeader) {
        AuthPrincipal principal = validarAuthorization(authorizationHeader);
        return usuarios.buscarPorId(principal.usuarioId());
    }

    public Usuario requerirRol(String authorizationHeader, String... rolesPermitidos) {
        Usuario usuario = usuarioDesdeAuthorization(authorizationHeader);
        Set<String> permitidos = Arrays.stream(rolesPermitidos)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        if (!permitidos.contains(usuario.getRolSistema().toUpperCase())) {
            throw new ForbiddenException("Operacion permitida solo para roles: " + String.join(", ", permitidos) + ".");
        }

        return usuario;
    }

    public Usuario verificarEmail(String authorizationHeader) {
        Usuario usuario = usuarioDesdeAuthorization(authorizationHeader);
        usuario.verificarEmail();
        return usuarios.guardar(usuario);
    }

    public Usuario actualizarPerfil(String authorizationHeader,
                                    String region,
                                    String juegoPrincipal,
                                    Integer rango,
                                    Integer latencia,
                                    List<String> rolesPreferidos,
                                    String disponibilidad) {
        Usuario usuario = usuarioDesdeAuthorization(authorizationHeader);

        if (region != null && !region.isBlank()) {
            usuario.setRegion(region);
        }
        if (latencia != null) {
            usuario.setLatenciaPromedio(latencia);
        }
        if (disponibilidad != null) {
            usuario.setDisponibilidad(disponibilidad);
        }
        if (rolesPreferidos != null) {
            usuario.setRolesPreferidos(toRoles(rolesPreferidos));
        }
        if (juegoPrincipal != null && !juegoPrincipal.isBlank() && rango != null) {
            Map<String, Integer> rangos = new HashMap<>();
            rangos.put(juegoPrincipal, rango);
            usuario.setRangoPorJuego(rangos);
        }

        return usuarios.guardar(usuario);
    }

    private AuthPrincipal validarAuthorization(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization Bearer token requerido.");
        }
        try {
            return jwtService.validar(authorizationHeader.substring("Bearer ".length()));
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }

    private List<Rol> toRoles(List<String> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .filter(rol -> rol != null && !rol.isBlank())
                .map(Rol::new)
                .toList();
    }
}
