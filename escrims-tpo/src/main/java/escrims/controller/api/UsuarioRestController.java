package escrims.controller.api;

import escrims.domain.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    private final UsuarioApiRepository usuarios;

    public UsuarioRestController(UsuarioApiRepository usuarios) {
        this.usuarios = usuarios;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.UsuarioResponse crear(@RequestBody ApiDtos.CrearUsuarioRequest request) {
        Usuario usuario = new Usuario(
                request.username(),
                request.email(),
                request.passwordHash(),
                request.region()
        );

        usuario.setLatenciaPromedio(request.latencia());

        Map<String, Integer> rangos = new HashMap<>();
        rangos.put(request.juego(), request.rango());
        usuario.setRangoPorJuego(rangos);

        if (request.verificarEmail()) {
            usuario.verificarEmail();
        }

        return toResponse(usuarios.guardar(usuario));
    }

    @GetMapping
    public List<ApiDtos.UsuarioResponse> listar() {
        return usuarios.listar().stream()
                .map(this::toResponse)
                .toList();
    }

    private ApiDtos.UsuarioResponse toResponse(Usuario usuario) {
        return new ApiDtos.UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRegion(),
                usuario.getRangoPorJuego().keySet().stream().findFirst().orElse(""),
                usuario.getRangoPorJuego().values().stream().findFirst().orElse(0),
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
}
