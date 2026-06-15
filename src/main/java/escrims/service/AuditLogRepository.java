package escrims.service;

import escrims.domain.model.AuditLog;

import java.util.Collection;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    Collection<AuditLog> findAll();
}
