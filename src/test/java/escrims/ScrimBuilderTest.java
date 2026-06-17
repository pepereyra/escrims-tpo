package escrims;

import escrims.infra.events.DomainEventBus;
import escrims.domain.state.ScrimBuilder;
import escrims.domain.state.ScrimContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del PATRON BUILDER — ScrimBuilder.
 *
 * Verifica que:
 * 1. Un scrim construido con todos los campos válidos se crea correctamente.
 * 2. Cada invariante de negocio lanza IllegalStateException si se viola.
 * 3. La interfaz fluida (fluent) funciona correctamente.
 *
 * GRASP Creator: ScrimBuilder es el creador natural de ScrimContext.
 * SOLID SRP: ScrimBuilder tiene una sola responsabilidad — construir y validar ScrimContext.
 */
class ScrimBuilderTest {

    private DomainEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new DomainEventBus();
    }

    // ── Tests de construcción válida ─────────────────────────────────

    @Test
    @DisplayName("Builder: construir un scrim válido devuelve ScrimContext en estado BUSCANDO")
    void builderScrimValidoEstaBuscando() {
        ScrimContext scrim = scrimValido().build();
        assertNotNull(scrim);
        assertEquals("BUSCANDO", scrim.getState().getNombre());
    }

    @Test
    @DisplayName("Builder: los atributos del scrim construido coinciden con los configurados")
    void builderAtributosCorrectos() {
        ScrimContext scrim = scrimValido().build();
        assertEquals("Valorant", scrim.getJuego());
        assertEquals("2v2",      scrim.getFormato());
        assertEquals("SA",       scrim.getRegion());
        assertEquals(1400,       scrim.getRangoMin());
        assertEquals(1700,       scrim.getRangoMax());
        assertEquals(80,         scrim.getLatenciaMax());
        assertEquals(30,         scrim.getDuracionMinutos());
        assertEquals(4,          scrim.getCuposTotales());
    }

    @Test
    @DisplayName("Builder: el scrim construido tiene un UUID único no nulo")
    void builderIdNoNulo() {
        ScrimContext scrim = scrimValido().build();
        assertNotNull(scrim.getId());
    }

    @Test
    @DisplayName("Builder: dos scrims construidos tienen IDs distintos")
    void builderDosScrimsIdsDistintos() {
        ScrimContext s1 = scrimValido().build();
        ScrimContext s2 = scrimValido().build();
        assertNotEquals(s1.getId(), s2.getId());
    }

    // ── Tests de validación de campos obligatorios ───────────────────

    @Test
    @DisplayName("Builder: juego nulo lanza IllegalStateException")
    void builderJuegoNuloLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().juego(null).build()
        );
    }

    @Test
    @DisplayName("Builder: juego vacío lanza IllegalStateException")
    void builderJuegoVacioLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().juego("   ").build()
        );
    }

    @Test
    @DisplayName("Builder: formato nulo lanza IllegalStateException")
    void builderFormatoNuloLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().formato(null).build()
        );
    }

    @Test
    @DisplayName("Builder: región nula lanza IllegalStateException")
    void builderRegionNulaLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().region(null).build()
        );
    }

    @Test
    @DisplayName("Builder: fechaHora nula lanza IllegalStateException")
    void builderFechaHoraNulaLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().fechaHora(null).build()
        );
    }

    // ── Tests de validación de rangos ────────────────────────────────

    @Test
    @DisplayName("Builder: rangoMin >= rangoMax lanza IllegalStateException")
    void builderRangoMinMayorQueMaxLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().rango(1700, 1400).build()
        );
    }

    @Test
    @DisplayName("Builder: rangoMin igual a rangoMax lanza IllegalStateException")
    void builderRangoMinIgualAMaxLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().rango(1500, 1500).build()
        );
    }

    @Test
    @DisplayName("Builder: latenciaMax <= 0 lanza IllegalStateException")
    void builderLatenciaMaxCeroLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().latenciaMax(0).build()
        );
    }

    @Test
    @DisplayName("Builder: latenciaMax negativa lanza IllegalStateException")
    void builderLatenciaMaxNegativaLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().latenciaMax(-10).build()
        );
    }

    // ── Tests de validación de duración y cupos ──────────────────────

    @Test
    @DisplayName("Builder: duracionMinutos <= 0 lanza IllegalStateException")
    void builderDuracionCeroLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().duracionMinutos(0).build()
        );
    }

    @Test
    @DisplayName("Builder: cuposTotales < 2 lanza IllegalStateException")
    void builderCuposMenorA2LanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().cuposTotales(1).build()
        );
    }

    @Test
    @DisplayName("Builder: cuposTotales impar lanza IllegalStateException (equipos desiguales)")
    void builderCuposImparLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().cuposTotales(3).build()
        );
    }

    @Test
    @DisplayName("Builder: cuposTotales par válido para el formato no lanza excepción")
    void builderCuposParValidoNoLanzaExcepcion() {
        assertDoesNotThrow(() -> scrimValido().formato("5v5").cuposTotales(10).build());
    }

    @Test
    @DisplayName("Builder: Valorant 2v2 requiere 4 cupos totales")
    void builderValorantValidaCuposContraFormato() {
        IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                scrimValido().formato("2v2").cuposTotales(10).build()
        );

        assertTrue(error.getMessage().contains("requiere 4 cupos"));
    }

    @Test
    @DisplayName("Builder: LoL no permite formato 3v3")
    void builderLolRechazaFormatoNoPermitido() {
        IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                scrimValido().juego("LoL").formato("3v3").cuposTotales(6).build()
        );

        assertTrue(error.getMessage().contains("no esta permitido"));
    }

    // ── Tests de validación de fecha ─────────────────────────────────

    @Test
    @DisplayName("Builder: fechaHora en el pasado lanza IllegalStateException")
    void builderFechaEnPasadoLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                scrimValido().fechaHora(LocalDateTime.now().minusHours(1)).build()
        );
    }

    @Test
    @DisplayName("Builder: eventBus nulo en constructor lanza IllegalArgumentException")
    void builderEventBusNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                new ScrimBuilder(null)
        );
    }

    // ── Helper ───────────────────────────────────────────────────────

    /**
     * Devuelve un ScrimBuilder preconfigurado con valores válidos.
     * Cada test puede sobreescribir el campo que quiere invalidar.
     */
    private ScrimBuilder scrimValido() {
        return new ScrimBuilder(eventBus)
                .juego("Valorant")
                .formato("2v2")
                .region("SA")
                .rango(1400, 1700)
                .latenciaMax(80)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .duracionMinutos(30)
                .cuposTotales(4);
    }
}
