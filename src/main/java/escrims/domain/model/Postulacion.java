package escrims.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Postulacion {

    private final UUID id;
    private final Usuario usuario;
    private Rol rolDeseado;
    private EstadoPostulacion estado;
    private final LocalDateTime fechaPostulacion;

    public Postulacion(Usuario usuario, Rol rolDeseado) {
        this(UUID.randomUUID(), usuario, rolDeseado, "PENDIENTE", LocalDateTime.now());
    }

    public Postulacion(UUID id,
                       Usuario usuario,
                       Rol rolDeseado,
                       String estado,
                       LocalDateTime fechaPostulacion) {
        this.id = id;
        this.usuario = usuario;
        this.rolDeseado = rolDeseado;
        this.estado = crearEstado(estado);
        this.fechaPostulacion = fechaPostulacion == null ? LocalDateTime.now() : fechaPostulacion;
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

    public void marcarSuplente() {
        this.estado = new SuplentePostulacion();
    }

    public void restaurar(Rol rol, String estado) {
        cambiarRol(rol);
        this.estado = crearEstado(estado);
    }

    public void cambiarRol(Rol nuevoRol) {
        if (nuevoRol == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }

        this.rolDeseado = nuevoRol;
    }

    public boolean estaAceptada() {
        return this.estado.estaAceptada();
    }

    private EstadoPostulacion crearEstado(String estado) {
        if ("ACEPTADA".equals(estado)) {
            return new AceptadaPostulacion();
        }
        if ("RECHAZADA".equals(estado)) {
            return new RechazadaPostulacion();
        }
        if ("SUPLENTE".equals(estado)) {
            return new SuplentePostulacion();
        }
        return new PendientePostulacion();
    }

    @Override
    public String toString() {
        return "Postulacion{usuario='" + usuario.getUsername() + "', rol=" + rolDeseado + ", estado=" + estado + "}";
    }
}
