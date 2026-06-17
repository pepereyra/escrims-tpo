package escrims.infra.persistence;

import escrims.domain.model.AlertaBusqueda;
import escrims.domain.model.Usuario;
import escrims.service.AlertaBusquedaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

@Repository
public class JpaAlertaBusquedaRepositoryAdapter implements AlertaBusquedaRepository {

    private final SpringDataAlertaBusquedaJpaRepository repository;

    public JpaAlertaBusquedaRepositoryAdapter(SpringDataAlertaBusquedaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public AlertaBusqueda save(AlertaBusqueda alerta) {
        repository.save(toEntity(alerta));
        return alerta;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<AlertaBusqueda> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(this::toDomain)
                .toList();
    }

    private AlertaBusquedaJpaEntity toEntity(AlertaBusqueda alerta) {
        Usuario usuario = alerta.getUsuario();
        return new AlertaBusquedaJpaEntity(
                alerta.getId(),
                alerta.getBusquedaId(),
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRegion(),
                usuario.getPasswordHash(),
                usuario.getRolSistema(),
                alerta.getScrimId(),
                alerta.getMensaje(),
                alerta.getFechaCreacion()
        );
    }

    private AlertaBusqueda toDomain(AlertaBusquedaJpaEntity entity) {
        Usuario usuario = new Usuario(
                entity.getUsuarioId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRegionUsuario(),
                true,
                0,
                null,
                entity.getRolSistema()
        );

        return new AlertaBusqueda(
                entity.getId(),
                entity.getBusquedaId(),
                usuario,
                entity.getScrimId(),
                entity.getMensaje(),
                entity.getFechaCreacion()
        );
    }
}
