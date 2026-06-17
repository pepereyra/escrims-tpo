package escrims.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Feedback {

    private final UUID id;
    private final UUID scrimId;
    private final Usuario autor;
    private final Usuario destinatario;
    private final int rating;
    private final String comentario;
    private EstadoModeracion estado;
    private final LocalDateTime fechaCreacion;

    public Feedback(UUID scrimId, Usuario autor, Usuario destinatario, int rating, String comentario) {
        this(UUID.randomUUID(), scrimId, autor, destinatario, rating, comentario, "PENDIENTE", LocalDateTime.now());
    }

    public Feedback(UUID id,
                    UUID scrimId,
                    Usuario autor,
                    Usuario destinatario,
                    int rating,
                    String comentario,
                    String estado,
                    LocalDateTime fechaCreacion) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("El rating debe estar entre 1 y 5.");
        }
        if (autor.getId().equals(destinatario.getId())) {
            throw new IllegalArgumentException("Un usuario no puede calificarse a si mismo.");
        }

        this.id = id;
        this.scrimId = scrimId;
        this.autor = autor;
        this.destinatario = destinatario;
        this.rating = rating;
        this.comentario = comentario == null ? "" : comentario;
        this.estado = crearEstado(estado);
        this.fechaCreacion = fechaCreacion == null ? LocalDateTime.now() : fechaCreacion;
    }

    public void aprobar() {
        this.estado = new AprobadoModeracion();
    }

    public void rechazar() {
        this.estado = new RechazadoModeracion();
    }

    public UUID getId() {
        return id;
    }

    public UUID getScrimId() {
        return scrimId;
    }

    public Usuario getAutor() {
        return autor;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public int getRating() {
        return rating;
    }

    public String getComentario() {
        return comentario;
    }

    public EstadoModeracion getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    private EstadoModeracion crearEstado(String estado) {
        if ("APROBADO".equals(estado)) {
            return new AprobadoModeracion();
        }
        if ("RECHAZADO".equals(estado)) {
            return new RechazadoModeracion();
        }
        return new PendienteModeracion();
    }
}
