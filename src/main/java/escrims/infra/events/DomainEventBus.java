package escrims.infra.events;

import java.util.ArrayList;
import java.util.List;

/**
 * PATRON OBSERVER - Subject (publicador de eventos).
 * Mantiene la lista de suscriptores y les notifica cuando se publica un evento.
 * GRASP Creator: crea y gestiona la relación con los Subscriber.
 * GRASP High Cohesion: solo se encarga de gestionar suscripciones y publicaciones.
 */
public class DomainEventBus {

    private final List<Subscriber> subscribers;

    public DomainEventBus() {
        this.subscribers = new ArrayList<>();
    }

    public void subscribe(Subscriber s) {
        if (!subscribers.contains(s)) {
            subscribers.add(s);
            System.out.println("[EventBus] Suscriptor registrado: " + s.getClass().getSimpleName());
        }
    }

    public void unsubscribe(Subscriber s) {
        subscribers.remove(s);
        System.out.println("[EventBus] Suscriptor removido: " + s.getClass().getSimpleName());
    }

    public void publish(DomainEvent evento) {
        System.out.println("[EventBus] Publicando evento: " + evento.getTipo() + " para scrim " + evento.getScrimId());
        for (Subscriber s : subscribers) {
            s.onEvent(evento);
        }
    }

    public int cantidadSuscriptores() {
        return subscribers.size();
    }
}
