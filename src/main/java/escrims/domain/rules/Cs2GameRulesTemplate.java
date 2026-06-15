package escrims.domain.rules;

import java.util.List;

public class Cs2GameRulesTemplate extends GameRulesTemplate {

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
}
