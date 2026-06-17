package escrims.domain.model;

/**
 * Estado simple de una notificación. Reemplaza la enumeración prohibida y mantiene
 * el modelo abierto a comportamiento futuro sin agregar complejidad hoy.
 */
public interface EstadoNotificacion {

    String getNombre();
}

class PendienteNotificacion implements EstadoNotificacion {
    @Override
    public String getNombre() {
        return "PENDIENTE";
    }

    @Override
    public String toString() {
        return getNombre();
    }
}

class EnviadaNotificacion implements EstadoNotificacion {
    @Override
    public String getNombre() {
        return "ENVIADA";
    }

    @Override
    public String toString() {
        return getNombre();
    }
}

class FallidaNotificacion implements EstadoNotificacion {
    @Override
    public String getNombre() {
        return "FALLIDA";
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
