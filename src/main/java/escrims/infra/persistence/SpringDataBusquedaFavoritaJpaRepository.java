package escrims.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataBusquedaFavoritaJpaRepository extends JpaRepository<BusquedaFavoritaJpaEntity, UUID> {

    List<BusquedaFavoritaJpaEntity> findByUsuarioId(UUID usuarioId);
}
