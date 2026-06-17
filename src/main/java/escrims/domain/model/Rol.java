package escrims.domain.model;

import java.util.Objects;

/**
 * Dato de dominio que representa el rol elegido por un jugador.
 *
 * No es Strategy porque no encapsula un algoritmo: solo identifica una
 * preferencia o posición dentro de un juego. Reemplaza la enumeración prohibida por
 * la consigna y permite crear roles nuevos sin modificar esta clase.
 */
public final class Rol {

    public static final Rol DUELIST = new Rol("DUELIST");
    public static final Rol CONTROLLER = new Rol("CONTROLLER");
    public static final Rol SENTINEL = new Rol("SENTINEL");
    public static final Rol INITIATOR = new Rol("INITIATOR");
    public static final Rol SUPPORT = new Rol("SUPPORT");
    public static final Rol JUNGLA = new Rol("JUNGLA");
    public static final Rol TOP = new Rol("TOP");
    public static final Rol MID = new Rol("MID");
    public static final Rol ADC = new Rol("ADC");

    private final String nombre;
    private final String juego;

    public Rol(String nombre) {
        this(nombre, null);
    }

    public Rol(String nombre, String juego) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio.");
        }

        this.nombre = nombre.trim().toUpperCase();
        this.juego = juego;
    }

    public String getNombre() {
        return nombre;
    }

    public String getJuego() {
        return juego;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Rol rol)) {
            return false;
        }
        return nombre.equals(rol.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
