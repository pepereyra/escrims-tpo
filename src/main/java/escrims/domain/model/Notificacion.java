package escrims.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notificacion {

    private final UUID id;
    private final String tipo;
    private final String canal;
    private final String payload;
    private EstadoNotificacion estado;
    private final LocalDateTime fechaCreacion;

    public Notificacion(String tipo, String canal, String payload) {
        this.id = UUID.randomUUID();
        this.tipo = tipo;
        this.canal = canal;
        this.payload = payload;
        this.estado = new PendienteNotificacion();
        this.fechaCreacion = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCanal() {
        return canal;
    }

    public String getPayload() {
        return payload;
    }

    public EstadoNotificacion getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void marcarEnviada() {
        this.estado = new EnviadaNotificacion();
    }

    public void marcarFallida() {
        this.estado = new FallidaNotificacion();
    }

    @Override
    public String toString() {
        return "Notificacion{tipo=" + tipo + ", canal=" + canal + ", estado=" + estado + "}";
    }
}
