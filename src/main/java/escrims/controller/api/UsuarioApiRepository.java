package escrims.controller.api;

import escrims.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UsuarioApiRepository {

    private final Map<String, Usuario> usuariosPorUsername = new ConcurrentHashMap<>();

    public Usuario guardar(Usuario usuario) {
        usuariosPorUsername.put(usuario.getUsername(), usuario);
        return usuario;
    }

    public Usuario buscar(String username) {
        Usuario usuario = usuariosPorUsername.get(username);

        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado: " + username);
        }

        return usuario;
    }

    public Collection<Usuario> listar() {
        return usuariosPorUsername.values();
    }
}
