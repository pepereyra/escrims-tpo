package escrims.infra.persistence;

import escrims.domain.model.ReporteConducta;
import escrims.service.ReporteConductaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaReporteConductaRepositoryAdapter implements ReporteConductaRepository {

    private final SpringDataReporteConductaJpaRepository reporteRepository;
    private final SpringDataScrimJpaRepository scrimRepository;
    private final SpringDataUsuarioJpaRepository usuarioRepository;

    public JpaReporteConductaRepositoryAdapter(SpringDataReporteConductaJpaRepository reporteRepository,
                                               SpringDataScrimJpaRepository scrimRepository,
                                               SpringDataUsuarioJpaRepository usuarioRepository) {
        this.reporteRepository = reporteRepository;
        this.scrimRepository = scrimRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public ReporteConducta save(ReporteConducta reporte) {
        reporteRepository.save(toEntity(reporte));
        return reporte;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReporteConducta> findById(UUID reporteId) {
        return reporteRepository.findById(reporteId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ReporteConducta> findByScrimId(UUID scrimId) {
        return reporteRepository.findByScrimId(scrimId).stream()
                .map(this::toDomain)
                .toList();
    }

    private ReporteConductaJpaEntity toEntity(ReporteConducta reporte) {
        return new ReporteConductaJpaEntity(
                reporte.getId(),
                scrimRepository.findById(reporte.getScrimId()).orElseThrow(),
                usuarioRepository.save(UsuarioJpaEntity.fromDomain(reporte.getReportante())),
                usuarioRepository.save(UsuarioJpaEntity.fromDomain(reporte.getReportado())),
                reporte.getMotivo(),
                reporte.getEstado().getNombre(),
                reporte.getSancion(),
                reporte.getEtapaResolucion(),
                reporte.getFechaCreacion()
        );
    }

    private ReporteConducta toDomain(ReporteConductaJpaEntity entity) {
        return new ReporteConducta(
                entity.getId(),
                entity.getScrim().getId(),
                entity.getReportante().toDomain(),
                entity.getReportado().toDomain(),
                entity.getMotivo(),
                entity.getEstado(),
                entity.getSancion(),
                entity.getEtapaResolucion(),
                entity.getFechaCreacion()
        );
    }
}
