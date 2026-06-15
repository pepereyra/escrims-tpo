package escrims.domain.rules;

import escrims.domain.model.Rol;
import escrims.domain.state.ScrimContext;

import java.util.List;

public class LeagueOfLegendsGameRulesStrategy implements GameRulesStrategy {

    @Override
    public String getJuego() {
        return "LoL";
    }

    @Override
    public List<String> formatosPermitidos() {
        return List.of("1V1", "2V2", "5V5");
    }

    @Override
    public List<String> rolesPermitidos() {
        return List.of("TOP", "JUNGLA", "MID", "ADC", "SUPPORT");
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
