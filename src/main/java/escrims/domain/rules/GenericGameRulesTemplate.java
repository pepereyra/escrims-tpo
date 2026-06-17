package escrims.domain.rules;

import java.util.List;

public class GenericGameRulesTemplate extends GameRulesTemplate {

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
}
