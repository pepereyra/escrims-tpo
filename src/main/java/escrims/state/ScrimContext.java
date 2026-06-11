package escrims.state;

import escrims.dominio.Confirmacion;
import escrims.dominio.Equipo;
import escrims.dominio.Postulacion;
import escrims.dominio.Usuario;
import escrims.dominio.enums.Rol;
import escrims.observer.DomainEvent;
import escrims.observer.DomainEventBus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PATRON STATE - Contexto del Scrim.
 * Mantiene referencia al estado actual y delega TODAS las operaciones en él.
 * GRASP Expert: conoce cupos, postulaciones y confirmaciones.
 * GRASP Low Coupling: no conoce los Notifier, solo al DomainEventBus.
 */
public class ScrimContext {

    private final UUID id;
    private final String juego;
    private final String formato;
    private final String region;
    private final int rangoMin;
    private final int rangoMax;
    private final int latenciaMax;
    private final LocalDateTime fechaHora;
    private final int duracionMinutos;
    private final int cuposTotales;
    private ScrimState state;
    private final List<Equipo> equipos;
    private final List<Postulacion> postulaciones;
    private final List<Confirmacion> confirmaciones;
    private final DomainEventBus eventBus;

    public ScrimContext(UUID id, String juego, String formato, String region,
                        int rangoMin, int rangoMax, int latenciaMax,
                        LocalDateTime fechaHora, int duracionMinutos, int cuposTotales,
                        DomainEventBus eventBus) {
        this.id = id;
        this.juego = juego;
        this.formato = formato;
        this.region = region;
        this.rangoMin = rangoMin;
        this.rangoMax = rangoMax;
        this.latenciaMax = latenciaMax;
        this.fechaHora = fechaHora;
        this.duracionMinutos = duracionMinutos;
        this.cuposTotales = cuposTotales;
        this.eventBus = eventBus;
        this.equipos = new ArrayList<>();
        this.postulaciones = new ArrayList<>();
        this.confirmaciones = new ArrayList<>();
        // Estado inicial siempre es BuscandoState
        this.state = new BuscandoState();
    }

    // ---- Delegación al estado actual ----

    public void postular(Usuario u, Rol rol) {
        state.postular(this, u, rol);
    }

    public void confirmar(Usuario u) {
        state.confirmar(this, u);
    }

    public void iniciar() {
        state.iniciar(this);
    }

    public void finalizar() {
        state.finalizar(this);
    }

    public void cancelar() {
        state.cancelar(this);
    }

    // ---- Gestión de estado (llamado desde los estados concretos) ----

    public void setState(ScrimState nuevoEstado) {
        System.out.println("[ScrimContext] Transición: " + state.getNombre() + " → " + nuevoEstado.getNombre());
        this.state = nuevoEstado;
    }

    // ---- Publicación de eventos ----

    public void publicarEvento(DomainEvent evento) {
        eventBus.publish(evento);
    }

    // ---- GRASP Expert: métodos que solo ScrimContext puede resolver ----

    public int cuposDisponibles() {
        long aceptadas = postulaciones.stream().filter(p -> p.estaAceptada()).count();
        return cuposTotales - (int) aceptadas;
    }

    public boolean todosConfirmaron() {
        if (confirmaciones.size() < cuposTotales) return false;
        return confirmaciones.stream().allMatch(c -> c.isConfirmado());
    }

    public boolean usuarioYaPostulado(Usuario u) {
        return postulaciones.stream()
                .anyMatch(p -> p.getUsuario().getId().equals(u.getId()));
    }

    public Confirmacion getConfirmacionDeUsuario(Usuario u) {
        return confirmaciones.stream()
                .filter(c -> c.getUsuario().getId().equals(u.getId()))
                .findFirst()
                .orElse(null);
    }

    // ---- Getters ----

    public UUID getId() { return id; }
    public String getJuego() { return juego; }
    public String getFormato() { return formato; }
    public String getRegion() { return region; }
    public int getRangoMin() { return rangoMin; }
    public int getRangoMax() { return rangoMax; }
    public int getLatenciaMax() { return latenciaMax; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public int getCuposTotales() { return cuposTotales; }
    public ScrimState getState() { return state; }
    public List<Equipo> getEquipos() { return equipos; }
    public List<Postulacion> getPostulaciones() { return postulaciones; }
    public List<Confirmacion> getConfirmaciones() { return confirmaciones; }

    @Override
    public String toString() {
        return "ScrimContext{juego='" + juego + "', estado='" + state.getNombre() +
               "', cuposDisponibles=" + cuposDisponibles() + "}";
    }
}
