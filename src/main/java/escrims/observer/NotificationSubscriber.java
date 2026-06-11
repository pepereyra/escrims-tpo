package escrims.observer;

import escrims.dominio.Notificacion;
import escrims.dominio.Usuario;
import escrims.dominio.enums.CanalNotificacion;
import escrims.strategy.NotificadorStrategy;

import java.util.List;

/**
 * PATRON OBSERVER + STRATEGY - Suscriptor que envía notificaciones.
 * Recibe eventos del DomainEventBus (Observer) y delega el envío
 * en la estrategia de notificación configurada (Strategy).
 *
 * GRASP Low Coupling: no conoce los estados del Scrim, solo reacciona a eventos.
 * SOLID DIP: depende de NotificadorStrategy (abstracción), no de Email/Push concretos.
 */
public class NotificationSubscriber implements Subscriber {

    private final List<Usuario> destinatarios;
    private final NotificadorStrategy notificador;
    private final CanalNotificacion canal;

    public NotificationSubscriber(List<Usuario> destinatarios,
                                   NotificadorStrategy notificador,
                                   CanalNotificacion canal) {
        this.destinatarios = destinatarios;
        this.notificador = notificador;
        this.canal = canal;
    }

    @Override
    public void onEvent(DomainEvent evento) {
        String payload = buildPayload(evento);
        for (Usuario u : destinatarios) {
            Notificacion notif = new Notificacion(evento.getTipo(), canal, payload);
            notificador.enviar(u, notif);
        }
    }

    private String buildPayload(DomainEvent evento) {
        return "Scrim [" + evento.getScrimId() + "] cambió a estado: " + evento.getTipo() +
               " el " + evento.getFecha();
    }

    public void agregarDestinatario(Usuario u) {
        if (!destinatarios.contains(u)) {
            destinatarios.add(u);
        }
    }

    public void removerDestinatario(Usuario u) {
        destinatarios.remove(u);
    }
}
