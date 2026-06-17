package escrims.service;

import escrims.domain.model.ReporteConducta;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ReporteConductaRepository {

    ReporteConducta save(ReporteConducta reporte);

    Optional<ReporteConducta> findById(UUID reporteId);

    Collection<ReporteConducta> findByScrimId(UUID scrimId);
}
