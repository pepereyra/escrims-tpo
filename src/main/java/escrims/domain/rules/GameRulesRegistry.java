package escrims.domain.rules;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GameRulesRegistry {

    private final Map<String, GameRulesTemplate> reglasPorJuego = new HashMap<>();
    private final List<GameRulesTemplate> reglasSoportadas = new ArrayList<>();
    private final GameRulesTemplate reglasGenericas = new GenericGameRulesTemplate();

    public GameRulesRegistry() {
        registrar(new ValorantGameRulesTemplate(), "VALORANT");
        registrar(new LeagueOfLegendsGameRulesTemplate(), "LOL", "LEAGUE OF LEGENDS", "LEAGUE_OF_LEGENDS");
        registrar(new Cs2GameRulesTemplate(), "CS2", "COUNTER STRIKE 2", "COUNTER-STRIKE 2");
    }

    public GameRulesTemplate obtenerPara(String juego) {
        return reglasPorJuego.getOrDefault(normalizar(juego), reglasGenericas);
    }

    public List<GameRulesTemplate> listarSoportadas() {
        return List.copyOf(reglasSoportadas);
    }

    private void registrar(GameRulesTemplate template, String... aliases) {
        reglasSoportadas.add(template);
        for (String alias : aliases) {
            reglasPorJuego.put(normalizar(alias), template);
        }
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
    }
}
