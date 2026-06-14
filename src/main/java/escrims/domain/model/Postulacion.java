package escrims.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Postulacion {

    private final UUID id;
    private final Usuario usuario;
    private final Rol rolDeseado;
    private EstadoPostulacion estado;
    private final LocalDateTime fechaPostulacion;

    public Postulacion(Usuario usuario, Rol rolDeseado) {
        this.id = UUID.randomUUID();
        this.usuario = usuario;
        this.rolDeseado = rolDeseado;
        this.estado = new PendientePostulacion();
        this.fechaPostulacion = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Rol getRolDeseado() {
        return rolDeseado;
    }

    public EstadoPostulacion getEstado() {
        return estado;
    }

    public LocalDateTime getFechaPostulacion() {
        return fechaPostulacion;
    }

    public void aceptar() {
        this.estado = new AceptadaPostulacion();
    }

    public void rechazar() {
        this.estado = new RechazadaPostulacion();
    }

    public boolean estaAceptada() {
        return this.estado.estaAceptada();
    }

    @Override
    public String toString() {
        return "Postulacion{usuario='" + usuario.getUsername() + "', rol=" + rolDeseado + ", estado=" + estado + "}";
    }
}
