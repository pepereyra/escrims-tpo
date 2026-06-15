package escrims.service;

import escrims.domain.model.BusquedaFavorita;

import java.util.Collection;
import java.util.UUID;

public interface BusquedaFavoritaRepository {

    BusquedaFavorita save(BusquedaFavorita busqueda);

    Collection<BusquedaFavorita> findAll();

    Collection<BusquedaFavorita> findByUsuarioId(UUID usuarioId);
}
