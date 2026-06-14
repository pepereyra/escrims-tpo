package escrims.service;

import escrims.domain.state.ScrimContext;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryScrimRepository implements ScrimRepository {

    private final Map<UUID, ScrimContext> scrims = new LinkedHashMap<>();

    @Override
    public ScrimContext save(ScrimContext scrim) {
        scrims.put(scrim.getId(), scrim);
        return scrim;
    }

    @Override
    public Optional<ScrimContext> findById(UUID scrimId) {
        return Optional.ofNullable(scrims.get(scrimId));
    }

    @Override
    public Collection<ScrimContext> findAll() {
        return scrims.values();
    }
}
