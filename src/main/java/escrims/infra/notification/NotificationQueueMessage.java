package escrims.infra.notification;

import escrims.domain.model.Notificacion;
import escrims.domain.model.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationQueueMessage(
        UUID usuarioId,
        String username,
        String email,
        String tipo,
        String canal,
        String payload,
        LocalDateTime fechaCreacion
) {

    public static NotificationQueueMessage from(Usuario usuario, Notificacion notificacion) {
        return new NotificationQueueMessage(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                notificacion.getTipo(),
                notificacion.getCanal(),
                notificacion.getPayload(),
                notificacion.getFechaCreacion()
        );
    }
}
