package escrims.infra.persistence;

import escrims.domain.model.Feedback;
import escrims.service.FeedbackRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaFeedbackRepositoryAdapter implements FeedbackRepository {

    private final SpringDataFeedbackJpaRepository feedbackRepository;
    private final SpringDataScrimJpaRepository scrimRepository;
    private final SpringDataUsuarioJpaRepository usuarioRepository;

    public JpaFeedbackRepositoryAdapter(SpringDataFeedbackJpaRepository feedbackRepository,
                                        SpringDataScrimJpaRepository scrimRepository,
                                        SpringDataUsuarioJpaRepository usuarioRepository) {
        this.feedbackRepository = feedbackRepository;
        this.scrimRepository = scrimRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public Feedback save(Feedback feedback) {
        feedbackRepository.save(toEntity(feedback));
        return feedback;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Feedback> findById(UUID feedbackId) {
        return feedbackRepository.findById(feedbackId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Feedback> findByScrimId(UUID scrimId) {
        return feedbackRepository.findByScrimId(scrimId).stream()
                .map(this::toDomain)
                .toList();
    }

    private FeedbackJpaEntity toEntity(Feedback feedback) {
        return new FeedbackJpaEntity(
                feedback.getId(),
                scrimRepository.findById(feedback.getScrimId()).orElseThrow(),
                usuarioRepository.save(UsuarioJpaEntity.fromDomain(feedback.getAutor())),
                usuarioRepository.save(UsuarioJpaEntity.fromDomain(feedback.getDestinatario())),
                feedback.getRating(),
                feedback.getComentario(),
                feedback.getEstado().getNombre(),
                feedback.getFechaCreacion()
        );
    }

    private Feedback toDomain(FeedbackJpaEntity entity) {
        return new Feedback(
                entity.getId(),
                entity.getScrim().getId(),
                entity.getAutor().toDomain(),
                entity.getDestinatario().toDomain(),
                entity.getRating(),
                entity.getComentario(),
                entity.getEstado(),
                entity.getFechaCreacion()
        );
    }
}
