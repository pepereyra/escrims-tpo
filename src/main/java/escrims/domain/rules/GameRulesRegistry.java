package escrims.domain.rules;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GameRulesRegistry {

    private final Map<String, GameRulesStrategy> reglasPorJuego = new HashMap<>();
    private final List<GameRulesStrategy> reglasSoportadas = new ArrayList<>();
    private final GameRulesStrategy reglasGenericas = new GenericGameRulesStrategy();

    public GameRulesRegistry() {
        registrar(new ValorantGameRulesStrategy(), "VALORANT");
        registrar(new LeagueOfLegendsGameRulesStrategy(), "LOL", "LEAGUE OF LEGENDS", "LEAGUE_OF_LEGENDS");
        registrar(new Cs2GameRulesStrategy(), "CS2", "COUNTER STRIKE 2", "COUNTER-STRIKE 2");
    }

    public GameRulesStrategy obtenerPara(String juego) {
        return reglasPorJuego.getOrDefault(normalizar(juego), reglasGenericas);
    }

    public List<GameRulesStrategy> listarSoportadas() {
        return List.copyOf(reglasSoportadas);
    }

    private void registrar(GameRulesStrategy strategy, String... aliases) {
        reglasSoportadas.add(strategy);
        for (String alias : aliases) {
            reglasPorJuego.put(normalizar(alias), strategy);
        }
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
    }
}
