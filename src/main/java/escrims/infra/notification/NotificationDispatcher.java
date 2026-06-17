package escrims.infra.notification;

import escrims.domain.model.Notificacion;
import escrims.domain.model.Usuario;

public interface NotificationDispatcher {

    void enqueue(Usuario destinatario, Notificacion notificacion, NotificadorStrategy notificador);

    int pendientes();
}
