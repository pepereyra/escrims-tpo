package escrims.observer;

/**
 * PATRON OBSERVER - Interfaz del observador.
 * Toda clase que quiera reaccionar a eventos de dominio debe implementar esta interfaz.
 */
public interface Subscriber {
    void onEvent(DomainEvent evento);
}
