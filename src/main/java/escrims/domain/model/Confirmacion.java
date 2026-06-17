package escrims.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Confirmacion {

    private final UUID id;
    private final Usuario usuario;
    private boolean confirmado;
    private LocalDateTime fechaConfirmacion;

    public Confirmacion(Usuario usuario) {
        this(UUID.randomUUID(), usuario, false, null);
    }

    public Confirmacion(UUID id, Usuario usuario, boolean confirmado, LocalDateTime fechaConfirmacion) {
        this.id = id;
        this.usuario = usuario;
        this.confirmado = confirmado;
        this.fechaConfirmacion = fechaConfirmacion;
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public void confirmar() {
        this.confirmado = true;
        this.fechaConfirmacion = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Confirmacion{usuario='" + usuario.getUsername() + "', confirmado=" + confirmado + "}";
    }
}
