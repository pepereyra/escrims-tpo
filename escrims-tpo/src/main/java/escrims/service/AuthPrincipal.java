package escrims.service;

import java.util.UUID;

public record AuthPrincipal(UUID usuarioId, String username, String rolSistema) {
}
