package escrims.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedback")
public class FeedbackJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scrim_id", nullable = false)
    private ScrimJpaEntity scrim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private UsuarioJpaEntity autor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private UsuarioJpaEntity destinatario;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 1000)
    private String comentario;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    protected FeedbackJpaEntity() {
    }

    public FeedbackJpaEntity(UUID id,
                             ScrimJpaEntity scrim,
                             UsuarioJpaEntity autor,
                             UsuarioJpaEntity destinatario,
                             int rating,
                             String comentario,
                             String estado,
                             LocalDateTime fechaCreacion) {
        this.id = id;
        this.scrim = scrim;
        this.autor = autor;
        this.destinatario = destinatario;
        this.rating = rating;
        this.comentario = comentario;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public UUID getId() {
        return id;
    }

    public ScrimJpaEntity getScrim() {
        return scrim;
    }

    public UsuarioJpaEntity getAutor() {
        return autor;
    }

    public UsuarioJpaEntity getDestinatario() {
        return destinatario;
    }

    public int getRating() {
        return rating;
    }

    public String getComentario() {
        return comentario;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
