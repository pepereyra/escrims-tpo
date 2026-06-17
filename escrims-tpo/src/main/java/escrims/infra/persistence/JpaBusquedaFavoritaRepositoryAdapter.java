package escrims.infra.persistence;

import escrims.domain.model.BusquedaFavorita;
import escrims.domain.model.Usuario;
import escrims.service.BusquedaFavoritaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

@Repository
public class JpaBusquedaFavoritaRepositoryAdapter implements BusquedaFavoritaRepository {

    private final SpringDataBusquedaFavoritaJpaRepository repository;

    public JpaBusquedaFavoritaRepositoryAdapter(SpringDataBusquedaFavoritaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public BusquedaFavorita save(BusquedaFavorita busqueda) {
        repository.save(toEntity(busqueda));
        return busqueda;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<BusquedaFavorita> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<BusquedaFavorita> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(this::toDomain)
                .toList();
    }

    private BusquedaFavoritaJpaEntity toEntity(BusquedaFavorita busqueda) {
        Usuario usuario = busqueda.getUsuario();
        return new BusquedaFavoritaJpaEntity(
                busqueda.getId(),
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRegion(),
                usuario.getPasswordHash(),
                usuario.getRolSistema(),
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

    private BusquedaFavorita toDomain(BusquedaFavoritaJpaEntity entity) {
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

        return new BusquedaFavorita(
                entity.getId(),
                usuario,
                entity.getJuego(),
                entity.getFormato(),
                entity.getRegion(),
                entity.getRangoMin(),
                entity.getRangoMax(),
                entity.getLatenciaMax(),
                entity.getFecha(),
                entity.getFechaCreacion()
        );
    }
}
