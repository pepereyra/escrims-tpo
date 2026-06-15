package escrims.domain.rules;

import escrims.domain.model.Rol;
import escrims.domain.state.ScrimContext;

import java.util.List;

public class Cs2GameRulesStrategy implements GameRulesStrategy {

    @Override
    public String getJuego() {
        return "CS2";
    }

    @Override
    public List<String> formatosPermitidos() {
        return List.of("1V1", "2V2", "5V5");
    }

    @Override
    public List<String> rolesPermitidos() {
        return List.of("IGL", "ENTRY", "AWP", "SUPPORT", "LURKER");
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
