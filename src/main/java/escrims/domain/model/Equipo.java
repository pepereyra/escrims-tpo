package escrims.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Equipo {

    private final UUID id;
    private String lado;
    private List<Usuario> jugadores;

    public Equipo(String lado) {
        this.id = UUID.randomUUID();
        this.lado = lado;
        this.jugadores = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public String getLado() {
        return lado;
    }

    public List<Usuario> getJugadores() {
        return jugadores;
    }

    public void agregarJugador(Usuario u) {
        if (!jugadores.contains(u)) {
            jugadores.add(u);
        }
    }

    public void removerJugador(Usuario u) {
        jugadores.remove(u);
    }

    public boolean estaCompleto(int cuposPorLado) {
        return jugadores.size() >= cuposPorLado;
    }

    public int cantidadJugadores() {
        return jugadores.size();
    }

    @Override
    public String toString() {
        return "Equipo{lado='" + lado + "', jugadores=" + jugadores.size() + "}";
    }
}
