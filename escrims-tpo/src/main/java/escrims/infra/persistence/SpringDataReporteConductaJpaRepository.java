package escrims.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataReporteConductaJpaRepository extends JpaRepository<ReporteConductaJpaEntity, UUID> {

    @Query("select r from ReporteConductaJpaEntity r where r.scrim.id = :scrimId")
    List<ReporteConductaJpaEntity> findByScrimId(@Param("scrimId") UUID scrimId);
}
