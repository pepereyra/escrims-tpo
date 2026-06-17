package escrims.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataFeedbackJpaRepository extends JpaRepository<FeedbackJpaEntity, UUID> {

    @Query("select f from FeedbackJpaEntity f where f.scrim.id = :scrimId")
    List<FeedbackJpaEntity> findByScrimId(@Param("scrimId") UUID scrimId);
}
