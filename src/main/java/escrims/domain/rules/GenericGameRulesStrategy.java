package escrims.domain.rules;

import escrims.domain.model.Rol;
import escrims.domain.state.ScrimContext;

import java.util.List;

public class GenericGameRulesStrategy implements GameRulesStrategy {

    @Override
    public String getJuego() {
        return "Generico";
    }

    @Override
    public List<String> formatosPermitidos() {
        return List.of("1V1", "2V2", "3V3", "4V4", "5V5");
    }

    @Override
    public List<String> rolesPermitidos() {
        return List.of();
    }

    @Override
    public void validarCreacion(String formato, int cuposTotales, String modalidad) {
        GameRulesValidator.validarFormato(this, formato);
        GameRulesValidator.validarCupos(this, formato, cuposTotales);
    }

    @Override
    public void validarPostulacion(ScrimContext scrim, Rol rol) {
        GameRulesValidator.validarRol(this, rol);
    }
}
