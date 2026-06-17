package escrims.controller.api;

import escrims.domain.model.AuditLog;
import escrims.service.AuthService;
import escrims.service.AuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditRestController {

    private final AuditService auditService;
    private final AuthService authService;

    public AuditRestController(AuditService auditService, AuthService authService) {
        this.auditService = auditService;
        this.authService = authService;
    }

    @GetMapping
    public List<ApiDtos.AuditLogResponse> listar(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requerirRol(authorization, "ADMIN");
        return auditService.listar().stream()
                .map(this::toResponse)
                .toList();
    }

    private ApiDtos.AuditLogResponse toResponse(AuditLog log) {
        return new ApiDtos.AuditLogResponse(
                log.getId(),
                log.getActor(),
                log.getAccion(),
                log.getEntidadTipo(),
                log.getEntidadId(),
                log.getDetalle(),
                log.getFecha()
        );
    }
}
