package escrims.service;

import escrims.domain.state.ScrimContext;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ScrimRepository {

    ScrimContext save(ScrimContext scrim);

    Optional<ScrimContext> findById(UUID scrimId);

    Collection<ScrimContext> findAll();
}
