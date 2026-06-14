package escrims.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataScrimJpaRepository extends JpaRepository<ScrimJpaEntity, UUID> {
}
