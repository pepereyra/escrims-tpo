package escrims.service;

import escrims.domain.model.Feedback;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepository {

    Feedback save(Feedback feedback);

    Optional<Feedback> findById(UUID feedbackId);

    Collection<Feedback> findByScrimId(UUID scrimId);
}
