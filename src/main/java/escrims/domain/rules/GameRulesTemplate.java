package escrims.domain.rules;

import escrims.domain.model.Rol;
import escrims.domain.state.ScrimContext;

import java.util.List;

public abstract class GameRulesTemplate {

    public final void validarCreacion(String formato, int cuposTotales, String modalidad) {
        validarFormato(formato);
        validarCupos(formato, cuposTotales);
        validarModalidad(modalidad);
        validarCreacionEspecifica(formato, cuposTotales, modalidad);
    }

    public final void validarPostulacion(ScrimContext scrim, Rol rol) {
        validarRol(rol);
        validarPostulacionEspecifica(scrim, rol);
    }

    public abstract String getJuego();

    public abstract List<String> formatosPermitidos();

    public abstract List<String> rolesPermitidos();

    protected void validarModalidad(String modalidad) {
    }

    protected void validarCreacionEspecifica(String formato, int cuposTotales, String modalidad) {
    }

    protected void validarPostulacionEspecifica(ScrimContext scrim, Rol rol) {
    }

    private void validarFormato(String formato) {
        GameRulesValidator.validarFormato(this, formato);
    }

    private void validarCupos(String formato, int cuposTotales) {
        GameRulesValidator.validarCupos(this, formato, cuposTotales);
    }

    private void validarRol(Rol rol) {
        GameRulesValidator.validarRol(this, rol);
    }
}
