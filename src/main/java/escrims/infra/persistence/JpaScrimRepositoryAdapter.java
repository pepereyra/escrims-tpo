package escrims.infra.persistence;

import escrims.domain.matchmaking.ByHistoryStrategy;
import escrims.domain.matchmaking.ByLatencyStrategy;
import escrims.domain.matchmaking.ByMMRStrategy;
import escrims.domain.matchmaking.CompositeMatchmakingStrategy;
import escrims.domain.model.Confirmacion;
import escrims.domain.model.Estadistica;
import escrims.domain.model.Postulacion;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.BuscandoState;
import escrims.domain.state.CanceladoState;
import escrims.domain.state.ConfirmadoState;
import escrims.domain.state.EnJuegoState;
import escrims.domain.state.FinalizadoState;
import escrims.domain.state.LobbyArmadoState;
import escrims.domain.state.ScrimContext;
import escrims.domain.state.ScrimState;
import escrims.infra.events.DomainEventBus;
import escrims.service.ScrimRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaScrimRepositoryAdapter implements ScrimRepository {

    private final SpringDataScrimJpaRepository scrimRepository;
    private final SpringDataUsuarioJpaRepository usuarioRepository;
    private final DomainEventBus eventBus;

    public JpaScrimRepositoryAdapter(SpringDataScrimJpaRepository scrimRepository,
                                     SpringDataUsuarioJpaRepository usuarioRepository,
                                     DomainEventBus eventBus) {
        this.scrimRepository = scrimRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventBus = eventBus;
    }

    @Override
    @Transactional
    public ScrimContext save(ScrimContext scrim) {
        scrimRepository.save(toEntity(scrim));
        return scrim;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScrimContext> findById(UUID scrimId) {
        return scrimRepository.findById(scrimId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ScrimContext> findAll() {
        return scrimRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private ScrimJpaEntity toEntity(ScrimContext scrim) {
        ScrimJpaEntity entity = scrimRepository.findById(scrim.getId())
                .orElseGet(() -> new ScrimJpaEntity(
                        scrim.getId(),
                        scrim.getJuego(),
                        scrim.getFormato(),
                        scrim.getRegion(),
                        scrim.getRangoMin(),
                        scrim.getRangoMax(),
                        scrim.getLatenciaMax(),
                        scrim.getFechaHora(),
                        scrim.getDuracionMinutos(),
                        scrim.getCuposTotales(),
                        scrim.getState().getNombre()
                ));

        entity.actualizar(
                scrim.getJuego(),
                scrim.getFormato(),
                scrim.getRegion(),
                scrim.getRangoMin(),
                scrim.getRangoMax(),
                scrim.getLatenciaMax(),
                scrim.getFechaHora(),
                scrim.getDuracionMinutos(),
                scrim.getCuposTotales(),
                scrim.getState().getNombre()
        );

        entity.reemplazarPostulaciones(scrim.getPostulaciones().stream()
                .map(this::toEntity)
                .toList());
        entity.reemplazarConfirmaciones(scrim.getConfirmaciones().stream()
                .map(this::toEntity)
                .toList());
        entity.reemplazarEstadisticas(scrim.getEstadisticas().stream()
                .map(this::toEntity)
                .toList());

        return entity;
    }

    private ScrimContext toDomain(ScrimJpaEntity entity) {
        ScrimContext scrim = new ScrimContext(
                entity.getId(),
                entity.getJuego(),
                entity.getFormato(),
                entity.getRegion(),
                entity.getRangoMin(),
                entity.getRangoMax(),
                entity.getLatenciaMax(),
                entity.getFechaHora(),
                entity.getDuracionMinutos(),
                entity.getCuposTotales(),
                eventBus,
                new CompositeMatchmakingStrategy(List.of(
                        new ByMMRStrategy(),
                        new ByLatencyStrategy(),
                        new ByHistoryStrategy()
                ))
        );

        entity.getPostulaciones().forEach(p -> scrim.getPostulaciones().add(toPostulacion(p)));
        entity.getConfirmaciones().forEach(c -> scrim.getConfirmaciones().add(toConfirmacion(c)));
        entity.getEstadisticas().forEach(e -> scrim.getEstadisticas().add(toEstadistica(e)));
        scrim.restaurarEstado(toState(entity.getEstado()));

        return scrim;
    }

    private PostulacionJpaEntity toEntity(Postulacion postulacion) {
        return new PostulacionJpaEntity(
                postulacion.getId(),
                toUsuarioEntity(postulacion.getUsuario()),
                postulacion.getRolDeseado().getNombre(),
                postulacion.getEstado().getNombre(),
                postulacion.getFechaPostulacion()
        );
    }

    private ConfirmacionJpaEntity toEntity(Confirmacion confirmacion) {
        return new ConfirmacionJpaEntity(
                confirmacion.getId(),
                toUsuarioEntity(confirmacion.getUsuario()),
                confirmacion.isConfirmado(),
                confirmacion.getFechaConfirmacion()
        );
    }

    private EstadisticaJpaEntity toEntity(Estadistica estadistica) {
        return new EstadisticaJpaEntity(
                estadistica.getId(),
                toUsuarioEntity(estadistica.getUsuario()),
                estadistica.isMvp(),
                estadistica.getKills(),
                estadistica.getDeaths(),
                estadistica.getAssists(),
                estadistica.getObservaciones()
        );
    }

    private UsuarioJpaEntity toUsuarioEntity(Usuario usuario) {
        return usuarioRepository.save(UsuarioJpaEntity.fromDomain(usuario));
    }

    private Postulacion toPostulacion(PostulacionJpaEntity entity) {
        return new Postulacion(
                entity.getId(),
                entity.getUsuario().toDomain(),
                new Rol(entity.getRolDeseado()),
                entity.getEstado(),
                entity.getFechaPostulacion()
        );
    }

    private Confirmacion toConfirmacion(ConfirmacionJpaEntity entity) {
        return new Confirmacion(
                entity.getId(),
                entity.getUsuario().toDomain(),
                entity.isConfirmado(),
                entity.getFechaConfirmacion()
        );
    }

    private Estadistica toEstadistica(EstadisticaJpaEntity entity) {
        return new Estadistica(
                entity.getId(),
                entity.getUsuario().toDomain(),
                entity.isMvp(),
                entity.getKills(),
                entity.getDeaths(),
                entity.getAssists(),
                entity.getObservaciones()
        );
    }

    private ScrimState toState(String estado) {
        if ("LOBBY_ARMADO".equals(estado)) {
            return new LobbyArmadoState();
        }
        if ("CONFIRMADO".equals(estado)) {
            return new ConfirmadoState();
        }
        if ("EN_JUEGO".equals(estado)) {
            return new EnJuegoState();
        }
        if ("FINALIZADO".equals(estado)) {
            return new FinalizadoState();
        }
        if ("CANCELADO".equals(estado)) {
            return new CanceladoState();
        }
        return new BuscandoState();
    }
}
