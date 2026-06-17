package escrims.domain.state;

import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;

/**
 * PATRON STATE - Interfaz del estado del Scrim.
 * Define todas las operaciones que pueden variar según el estado actual.
 * IMPORTANTE (según rúbrica):
 *   - NO tiene cambiarEstado(): las transiciones las decide cada estado concreto.
 *   - El ScrimContext se pasa como parámetro en cada método (no como campo).
 *   - Cada estado concreto implementa TODOS los métodos.
 */
public interface ScrimState {
    void postular(ScrimContext ctx, Usuario u, Rol rol);
    void confirmar(ScrimContext ctx, Usuario u);
    void iniciar(ScrimContext ctx);
    void finalizar(ScrimContext ctx);
    void cancelar(ScrimContext ctx);
    String getNombre();
}
