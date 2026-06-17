package escrims.domain.model;

/**
 * Estado simple de una postulación. Se modela con polimorfismo para evitar
 * enumeraciones, sin forzar un State completo porque hoy no hay transiciones complejas.
 */
public interface EstadoPostulacion {

    String getNombre();

    default boolean estaAceptada() {
        return false;
    }
}

class PendientePostulacion implements EstadoPostulacion {
    @Override
    public String getNombre() {
        return "PENDIENTE";
    }

    @Override
    public String toString() {
        return getNombre();
    }
}

class AceptadaPostulacion implements EstadoPostulacion {
    @Override
    public String getNombre() {
        return "ACEPTADA";
    }

    @Override
    public boolean estaAceptada() {
        return true;
    }

    @Override
    public String toString() {
        return getNombre();
    }
}

class RechazadaPostulacion implements EstadoPostulacion {
    @Override
    public String getNombre() {
        return "RECHAZADA";
    }

    @Override
    public String toString() {
        return getNombre();
    }
}

class SuplentePostulacion implements EstadoPostulacion {
    @Override
    public String getNombre() {
        return "SUPLENTE";
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
