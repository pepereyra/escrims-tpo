package escrims.infra.notification;

import escrims.domain.model.Notificacion;
import escrims.domain.model.Usuario;

import java.util.ArrayDeque;
import java.util.Queue;

public class QueuedNotificationDispatcher {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MILLIS = 200;

    private final Queue<NotificationJob> queue = new ArrayDeque<>();

    public void enqueue(Usuario destinatario, Notificacion notificacion, NotificadorStrategy notificador) {
        queue.add(new NotificationJob(destinatario, notificacion, notificador));
        drain();
    }

    public int pendientes() {
        return queue.size();
    }

    private void drain() {
        while (!queue.isEmpty()) {
            dispatch(queue.poll());
        }
    }

    private void dispatch(NotificationJob job) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                job.notificador.enviar(job.destinatario, job.notificacion);
                return;
            } catch (RuntimeException e) {
                long backoff = BASE_BACKOFF_MILLIS * (1L << (attempt - 1));
                System.out.println("[NotificationQueue] fallo intento " + attempt
                        + " canal=" + job.notificador.getCanal()
                        + " retryEnMs=" + backoff
                        + " error=" + e.getMessage());
            }
        }

        job.notificacion.marcarFallida();
    }

    private record NotificationJob(Usuario destinatario,
                                   Notificacion notificacion,
                                   NotificadorStrategy notificador) {
    }
}
