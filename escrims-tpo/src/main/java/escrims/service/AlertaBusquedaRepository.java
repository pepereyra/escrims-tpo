package escrims.service;

import escrims.domain.model.AlertaBusqueda;

import java.util.Collection;
import java.util.UUID;

public interface AlertaBusquedaRepository {

    AlertaBusqueda save(AlertaBusqueda alerta);

    Collection<AlertaBusqueda> findByUsuarioId(UUID usuarioId);
}
