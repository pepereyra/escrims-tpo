package escrims.domain.rules;

import escrims.domain.model.Rol;

import java.util.List;
import java.util.Locale;

final class GameRulesValidator {

    private GameRulesValidator() {
    }

    static void validarFormato(GameRulesStrategy strategy, String formato) {
        if (!strategy.formatosPermitidos().contains(normalizar(formato))) {
            throw new IllegalStateException(
                    "El formato " + formato + " no esta permitido para " + strategy.getJuego()
                            + ". Formatos permitidos: " + strategy.formatosPermitidos()
            );
        }
    }

    static void validarCupos(GameRulesStrategy strategy, String formato, int cuposTotales) {
        int cuposEsperados = cuposEsperadosPorFormato(formato);
        if (cuposEsperados > 0 && cuposTotales != cuposEsperados) {
            throw new IllegalStateException(
                    "El formato " + formato + " de " + strategy.getJuego()
                            + " requiere " + cuposEsperados + " cupos totales."
            );
        }
    }

    static void validarRol(GameRulesStrategy strategy, Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }

        if (!strategy.rolesPermitidos().isEmpty()
                && !strategy.rolesPermitidos().contains(normalizar(rol.getNombre()))) {
            throw new IllegalArgumentException(
                    "El rol " + rol.getNombre() + " no esta permitido para " + strategy.getJuego()
                            + ". Roles permitidos: " + strategy.rolesPermitidos()
            );
        }
    }

    static String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
    }

    private static int cuposEsperadosPorFormato(String formato) {
        String[] partes = normalizar(formato).split("V");
        if (partes.length != 2) {
            return -1;
        }

        try {
            int jugadoresPorLado = Integer.parseInt(partes[0]);
            return jugadoresPorLado * 2;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
