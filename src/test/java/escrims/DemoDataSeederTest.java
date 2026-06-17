package escrims;

import escrims.infra.persistence.SpringDataScrimJpaRepository;
import escrims.infra.persistence.SpringDataUsuarioJpaRepository;
import escrims.infra.persistence.SpringDataBusquedaFavoritaJpaRepository;
import escrims.infra.persistence.SpringDataFeedbackJpaRepository;
import escrims.infra.persistence.SpringDataReporteConductaJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "app.demo-data.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:demo-seeder-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class DemoDataSeederTest {

    @Autowired
    private SpringDataUsuarioJpaRepository usuarioRepository;

    @Autowired
    private SpringDataScrimJpaRepository scrimRepository;

    @Autowired
    private SpringDataBusquedaFavoritaJpaRepository busquedaRepository;

    @Autowired
    private SpringDataFeedbackJpaRepository feedbackRepository;

    @Autowired
    private SpringDataReporteConductaJpaRepository reporteRepository;

    @Test
    @DisplayName("DemoDataSeeder precarga datos masivos compatibles con reglas por juego")
    void demoDataSeederPrecargaDatosValidos() {
        assertTrue(usuarioRepository.existsByUsername("admin"));
        assertTrue(usuarioRepository.existsByUsername("echo"));
        assertTrue(usuarioRepository.count() >= 120);
        assertTrue(scrimRepository.count() >= 120);
        assertTrue(busquedaRepository.count() >= 120);
        assertTrue(feedbackRepository.count() >= 120);
        assertTrue(reporteRepository.count() >= 120);

        assertTrue(scrimRepository.findAll().stream()
                .anyMatch(scrim -> "Valorant".equals(scrim.getJuego())
                        && "2v2".equals(scrim.getFormato())
                        && "CONFIRMADO".equals(scrim.getEstado())));

        assertTrue(scrimRepository.findAll().stream()
                .anyMatch(scrim -> "LoL".equals(scrim.getJuego())
                        && "2v2".equals(scrim.getFormato())
                        && scrim.getCuposTotales() == 4
                        && "BUSCANDO".equals(scrim.getEstado())));

        assertTrue(scrimRepository.findAll().stream()
                .anyMatch(scrim -> "CS2".equals(scrim.getJuego())
                        && "2v2".equals(scrim.getFormato())));
    }
}
