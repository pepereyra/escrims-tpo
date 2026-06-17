package escrims.domain.model;

/**
 * Estado simple de moderacion para feedback y reportes.
 */
public interface EstadoModeracion {

    String getNombre();
}

class PendienteModeracion implements EstadoModeracion {
    @Override
    public String getNombre() {
        return "PENDIENTE";
    }
}

class AprobadoModeracion implements EstadoModeracion {
    @Override
    public String getNombre() {
        return "APROBADO";
    }
}

class RechazadoModeracion implements EstadoModeracion {
    @Override
    public String getNombre() {
        return "RECHAZADO";
    }
}
