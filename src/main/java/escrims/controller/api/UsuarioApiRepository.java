package escrims.controller.api;

import escrims.domain.model.Usuario;
import escrims.infra.persistence.SpringDataUsuarioJpaRepository;
import escrims.infra.persistence.UsuarioJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
public class UsuarioApiRepository {

    private final SpringDataUsuarioJpaRepository repository;

    public UsuarioApiRepository(SpringDataUsuarioJpaRepository repository) {
        this.repository = repository;
    }

    public Usuario guardar(Usuario usuario) {
        return repository.save(UsuarioJpaEntity.fromDomain(usuario)).toDomain();
    }

    public Usuario buscar(String username) {
        return repository.findByUsername(username)
                .map(UsuarioJpaEntity::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
    }

    public Usuario buscarPorId(UUID usuarioId) {
        return repository.findById(usuarioId)
                .map(UsuarioJpaEntity::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));
    }

    public boolean existeUsername(String username) {
        return repository.existsByUsername(username);
    }

    public boolean existeEmail(String email) {
        return repository.existsByEmail(email);
    }

    public Collection<Usuario> listar() {
        return repository.findAll().stream()
                .map(UsuarioJpaEntity::toDomain)
                .toList();
    }
}
