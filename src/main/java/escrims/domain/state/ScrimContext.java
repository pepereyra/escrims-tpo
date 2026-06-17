package escrims.domain.state;

import escrims.domain.matchmaking.MatchmakingStrategy;
import escrims.domain.model.Confirmacion;
import escrims.domain.model.Equipo;
import escrims.domain.model.Estadistica;
import escrims.domain.model.Postulacion;
import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.infra.events.DomainEvent;
import escrims.infra.events.DomainEventBus;
import escrims.infra.events.ScrimStateChangedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PATRON STATE - Contexto del Scrim.
 * Mantiene referencia al estado actual y delega TODAS las operaciones en él.
 *
 * GRASP Information Expert:
 * conoce cupos, postulaciones, confirmaciones y la estrategia de matchmaking.
 *
 * GRASP Low Coupling:
 * no conoce implementaciones concretas de notificación ni de emparejamiento.
 * Solo depende de DomainEventBus y MatchmakingStrategy.
 *
 * SOLID DIP:
 * depende de MatchmakingStrategy, no de estrategias concretas como
 * ByMMRStrategy, ByLatencyStrategy o ByHistoryStrategy.
 */
public class ScrimContext {

    private final UUID id;
    private final String juego;
    private final String formato;
    private final String region;
    private final int rangoMin;
    private final int rangoMax;
    private final int latenciaMax;
    private final LocalDateTime fechaHora;
    private final int duracionMinutos;
    private final int cuposTotales;
    private final String modalidad;

    private ScrimState state;

    private final List<Equipo> equipos;
    private final List<Postulacion> postulaciones;
    private final List<Confirmacion> confirmaciones;
    private final List<Estadistica> estadisticas;

    private final DomainEventBus eventBus;
    private final MatchmakingStrategy matchmakingStrategy;

    public ScrimContext(UUID id,
                        String juego,
                        String formato,
                        String region,
                        int rangoMin,
                        int rangoMax,
                        int latenciaMax,
                        LocalDateTime fechaHora,
                        int duracionMinutos,
                        int cuposTotales,
                        DomainEventBus eventBus,
                        MatchmakingStrategy matchmakingStrategy) {
        this(id, juego, formato, region, rangoMin, rangoMax, latenciaMax, fechaHora, duracionMinutos,
                cuposTotales, "CASUAL", eventBus, matchmakingStrategy);
    }

    public ScrimContext(UUID id,
                        String juego,
                        String formato,
                        String region,
                        int rangoMin,
                        int rangoMax,
                        int latenciaMax,
                        LocalDateTime fechaHora,
                        int duracionMinutos,
                        int cuposTotales,
                        String modalidad,
                        DomainEventBus eventBus,
                        MatchmakingStrategy matchmakingStrategy) {

        this.id = id;
        this.juego = juego;
        this.formato = formato;
        this.region = region;
        this.rangoMin = rangoMin;
        this.rangoMax = rangoMax;
        this.latenciaMax = latenciaMax;
        this.fechaHora = fechaHora;
        this.duracionMinutos = duracionMinutos;
        this.cuposTotales = cuposTotales;
        this.modalidad = modalidad == null || modalidad.isBlank() ? "CASUAL" : modalidad.toUpperCase();
        this.eventBus = eventBus;
        this.matchmakingStrategy = matchmakingStrategy;

        this.equipos = new ArrayList<>();
        this.postulaciones = new ArrayList<>();
        this.confirmaciones = new ArrayList<>();
        this.estadisticas = new ArrayList<>();
        recomponerEquipos();

        this.state = new BuscandoState();
    }

    public void postular(Usuario u, Rol rol) {
        state.postular(this, u, rol);
    }

    public void confirmar(Usuario u) {
        state.confirmar(this, u);
    }

    public void iniciar() {
        state.iniciar(this);
    }

    public void finalizar() {
        state.finalizar(this);
    }

    public void cancelar() {
        state.cancelar(this);
    }

    public void setState(ScrimState nuevoEstado) {
        System.out.println("[ScrimContext] Transición: "
                + state.getNombre() + " → " + nuevoEstado.getNombre());
        this.state = nuevoEstado;
    }

    public void restaurarEstado(ScrimState estadoPersistido) {
        this.state = estadoPersistido;
    }

    public void recomponerEquipos() {
        equipos.clear();
        Equipo equipoA = new Equipo("A");
        Equipo equipoB = new Equipo("B");

        List<Usuario> aceptados = postulaciones.stream()
                .filter(Postulacion::estaAceptada)
                .map(Postulacion::getUsuario)
                .toList();

        int cuposPorLado = cuposTotales / 2;
        for (int i = 0; i < aceptados.size(); i++) {
            if (i < cuposPorLado) {
                equipoA.agregarJugador(aceptados.get(i));
            } else {
                equipoB.agregarJugador(aceptados.get(i));
            }
        }

        equipos.add(equipoA);
        equipos.add(equipoB);
    }

    public void publicarEvento(DomainEvent evento) {
        eventBus.publish(evento);
    }

    public int cuposDisponibles() {
        long aceptadas = postulaciones.stream()
                .filter(Postulacion::estaAceptada)
                .count();

        return cuposTotales - (int) aceptadas;
    }

    public boolean todosConfirmaron() {
        if (confirmaciones.size() < cuposTotales) {
            return false;
        }

        return confirmaciones.stream()
                .allMatch(Confirmacion::isConfirmado);
    }

    public boolean usuarioYaPostulado(Usuario u) {
        return postulaciones.stream()
                .anyMatch(p -> p.getUsuario().getId().equals(u.getId()));
    }

    public Confirmacion getConfirmacionDeUsuario(Usuario u) {
        return confirmaciones.stream()
                .filter(c -> c.getUsuario().getId().equals(u.getId()))
                .findFirst()
                .orElse(null);
    }

    public void restaurarPostulacion(Usuario usuario, Rol rol, String estado) {
        postulacionDe(usuario).restaurar(rol, estado);
        recomponerEquipos();
    }

    public void restaurarConfirmacion(Confirmacion confirmacion) {
        if (confirmacion == null) {
            return;
        }
        boolean existe = confirmaciones.stream()
                .anyMatch(c -> c.getId().equals(confirmacion.getId()));
        if (!existe) {
            confirmaciones.add(confirmacion);
        }
    }

    public void removerConfirmacion(Usuario usuario) {
        confirmaciones.removeIf(c -> c.getUsuario().getId().equals(usuario.getId()));
    }

    public void cambiarRol(Usuario usuario, Rol nuevoRol) {
        validarGestionPreJuego();
        postulacionAceptadaDe(usuario).cambiarRol(nuevoRol);
    }

    public void intercambiarRoles(Usuario usuarioA, Usuario usuarioB) {
        validarGestionPreJuego();

        Postulacion postulacionA = postulacionAceptadaDe(usuarioA);
        Postulacion postulacionB = postulacionAceptadaDe(usuarioB);

        Rol rolA = postulacionA.getRolDeseado();
        postulacionA.cambiarRol(postulacionB.getRolDeseado());
        postulacionB.cambiarRol(rolA);
    }

    public void moverASuplente(Usuario usuario) {
        validarGestionPreJuego();

        Postulacion postulacion = postulacionAceptadaDe(usuario);
        postulacion.marcarSuplente();
        confirmaciones.removeIf(c -> c.getUsuario().getId().equals(usuario.getId()));

        if (cuposDisponibles() > 0 && !state.getNombre().equals("BUSCANDO")) {
            String estadoAnterior = state.getNombre();
            setState(new BuscandoState());
            publicarEvento(new ScrimStateChangedEvent(
                    id,
                    estadoAnterior,
                    "BUSCANDO"
            ));
        }

        recomponerEquipos();
    }

    public void reactivarTitular(Usuario usuario) {
        validarGestionPreJuego();

        if (cuposDisponibles() <= 0) {
            throw new IllegalStateException("No hay cupos disponibles para reactivar un suplente.");
        }

        Postulacion postulacion = postulacionDe(usuario);
        if (!"SUPLENTE".equals(postulacion.getEstado().getNombre())) {
            throw new IllegalArgumentException(
                    "El usuario " + usuario.getUsername() + " no esta como suplente en este scrim."
            );
        }

        postulacion.aceptar();
        if (getConfirmacionDeUsuario(usuario) == null) {
            confirmaciones.add(new Confirmacion(usuario));
        }

        if (cuposDisponibles() == 0 && state.getNombre().equals("BUSCANDO")) {
            setState(new LobbyArmadoState());
            publicarEvento(new ScrimStateChangedEvent(
                    id,
                    "BUSCANDO",
                    "LOBBY_ARMADO"
            ));
        }

        recomponerEquipos();
    }

    private Postulacion postulacionAceptadaDe(Usuario usuario) {
        return postulacionDe(usuario, true);
    }

    private Postulacion postulacionDe(Usuario usuario) {
        return postulacionDe(usuario, false);
    }

    private Postulacion postulacionDe(Usuario usuario, boolean soloAceptada) {
        return postulaciones.stream()
                .filter(p -> !soloAceptada || p.estaAceptada())
                .filter(p -> p.getUsuario().getId().equals(usuario.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario " + usuario.getUsername()
                                + (soloAceptada
                                ? " no tiene una postulacion aceptada en este scrim."
                                : " no tiene una postulacion en este scrim.")
                ));
    }

    private void validarGestionPreJuego() {
        String estado = state.getNombre();
        if (estado.equals("EN_JUEGO") || estado.equals("FINALIZADO") || estado.equals("CANCELADO")) {
            throw new IllegalStateException(
                    "No se pueden gestionar roles o suplentes en estado " + estado + "."
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public String getJuego() {
        return juego;
    }

    public String getFormato() {
        return formato;
    }

    public String getRegion() {
        return region;
    }

    public int getRangoMin() {
        return rangoMin;
    }

    public int getRangoMax() {
        return rangoMax;
    }

    public int getLatenciaMax() {
        return latenciaMax;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public int getCuposTotales() {
        return cuposTotales;
    }

    public String getModalidad() {
        return modalidad;
    }

    public ScrimState getState() {
        return state;
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }

    public List<Postulacion> getPostulaciones() {
        return postulaciones;
    }

    public List<Confirmacion> getConfirmaciones() {
        return confirmaciones;
    }

    public List<Estadistica> getEstadisticas() {
        return estadisticas;
    }

    public MatchmakingStrategy getMatchmakingStrategy() {
        return matchmakingStrategy;
    }

    @Override
    public String toString() {
        return "ScrimContext{juego='" + juego
                + "', estado='" + state.getNombre()
                + "', cuposDisponibles=" + cuposDisponibles()
                + "}";
    }
}
