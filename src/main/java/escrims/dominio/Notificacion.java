package escrims.dominio;

import escrims.dominio.enums.CanalNotificacion;
import escrims.dominio.enums.EstadoNotificacion;
import escrims.dominio.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notificacion {

    private final UUID id;
    private final TipoEvento tipo;
    private final CanalNotificacion canal;
    private final String payload;
    private EstadoNotificacion estado;
    private final LocalDateTime fechaCreacion;

    public Notificacion(TipoEvento tipo, CanalNotificacion canal, String payload) {
        this.id = UUID.randomUUID();
        this.tipo = tipo;
        this.canal = canal;
        this.payload = payload;
        this.estado = EstadoNotificacion.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public CanalNotificacion getCanal() {
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
        this.estado = EstadoNotificacion.ENVIADA;
    }

    public void marcarFallida() {
        this.estado = EstadoNotificacion.FALLIDA;
    }

    @Override
    public String toString() {
        return "Notificacion{tipo=" + tipo + ", canal=" + canal + ", estado=" + estado + "}";
    }
}
