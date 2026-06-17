package escrims.controller.api;

import escrims.domain.rules.GameRulesRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoRestController {

    private final GameRulesRegistry gameRulesRegistry = new GameRulesRegistry();

    @GetMapping
    public ApiDtos.CatalogosResponse catalogos() {
        return new ApiDtos.CatalogosResponse(
                gameRulesRegistry.listarSoportadas().stream()
                        .map(rules -> new ApiDtos.JuegoCatalogoResponse(
                                rules.getJuego(),
                                rules.formatosPermitidos(),
                                rules.rolesPermitidos()
                        ))
                        .toList(),
                List.of("RANKED_LIKE", "CASUAL", "PRACTICA")
        );
    }
}
