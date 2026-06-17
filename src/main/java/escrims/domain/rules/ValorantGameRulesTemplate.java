package escrims.domain.rules;

import java.util.List;

public class ValorantGameRulesTemplate extends GameRulesTemplate {

    @Override
    public String getJuego() {
        return "Valorant";
    }

    @Override
    public List<String> formatosPermitidos() {
        return List.of("1V1", "2V2", "3V3", "5V5");
    }

    @Override
    public List<String> rolesPermitidos() {
        return List.of("DUELIST", "CONTROLLER", "SENTINEL", "INITIATOR", "IGL", "SUPPORT");
    }
}
