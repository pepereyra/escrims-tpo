package escrims.domain.rules;

import escrims.domain.model.Rol;
import escrims.domain.state.ScrimContext;

import java.util.List;

public interface GameRulesStrategy {

    String getJuego();

    List<String> formatosPermitidos();

    List<String> rolesPermitidos();

    void validarCreacion(String formato, int cuposTotales, String modalidad);

    void validarPostulacion(ScrimContext scrim, Rol rol);
}
