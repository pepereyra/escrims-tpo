package escrims.service;

import escrims.domain.model.AlertaBusqueda;
import escrims.domain.model.BusquedaFavorita;
import escrims.domain.model.Usuario;
import escrims.infra.events.DomainEvent;
import escrims.infra.events.ScrimCreadoEvent;
import escrims.infra.events.Subscriber;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

public class BusquedaFavoritaService implements Subscriber {

    private final BusquedaFavoritaRepository busquedas;
    private final AlertaBusquedaRepository alertas;

    public BusquedaFavoritaService(BusquedaFavoritaRepository busquedas,
                                   AlertaBusquedaRepository alertas) {
        this.busquedas = busquedas;
        this.alertas = alertas;
    }

    public BusquedaFavorita guardar(Usuario usuario,
                                    String juego,
                                    String formato,
                                    String region,
                                    Integer rangoMin,
                                    Integer rangoMax,
                                    Integer latenciaMax,
                                    LocalDate fecha) {
        return busquedas.save(new BusquedaFavorita(
                usuario,
                juego,
                formato,
                region,
                rangoMin,
                rangoMax,
                latenciaMax,
                fecha
        ));
    }

    public Collection<BusquedaFavorita> listarBusquedas(UUID usuarioId) {
        return busquedas.findByUsuarioId(usuarioId);
    }

    public Collection<AlertaBusqueda> listarAlertas(UUID usuarioId) {
        return alertas.findByUsuarioId(usuarioId);
    }

    @Override
    public void onEvent(DomainEvent evento) {
        if (!(evento instanceof ScrimCreadoEvent scrimCreado)) {
            return;
        }

        busquedas.findAll().stream()
                .filter(busqueda -> busqueda.coincideCon(scrimCreado))
                .map(busqueda -> new AlertaBusqueda(
                        busqueda.getId(),
                        busqueda.getUsuario(),
                        scrimCreado.getScrimId(),
                        "Nuevo scrim compatible: " + scrimCreado.getJuego()
                                + " " + scrimCreado.getFormato()
                                + " en " + scrimCreado.getRegion()
                ))
                .forEach(alertas::save);
    }
}
