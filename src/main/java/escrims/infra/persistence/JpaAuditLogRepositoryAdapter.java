package escrims.infra.persistence;

import escrims.domain.model.AuditLog;
import escrims.service.AuditLogRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
public class JpaAuditLogRepositoryAdapter implements AuditLogRepository {

    private final SpringDataAuditLogJpaRepository repository;

    public JpaAuditLogRepositoryAdapter(SpringDataAuditLogJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public AuditLog save(AuditLog auditLog) {
        repository.save(toEntity(auditLog));
        return auditLog;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<AuditLog> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private AuditLogJpaEntity toEntity(AuditLog auditLog) {
        return new AuditLogJpaEntity(
                auditLog.getId(),
                auditLog.getActor(),
                auditLog.getAccion(),
                auditLog.getEntidadTipo(),
                auditLog.getEntidadId(),
                auditLog.getDetalle(),
                auditLog.getFecha()
        );
    }

    private AuditLog toDomain(AuditLogJpaEntity entity) {
        return new AuditLog(
                entity.getId(),
                entity.getActor(),
                entity.getAccion(),
                entity.getEntidadTipo(),
                entity.getEntidadId(),
                entity.getDetalle(),
                entity.getFecha()
        );
    }
}
