package escrims;

import escrims.infra.persistence.SpringDataScrimJpaRepository;
import escrims.infra.persistence.SpringDataUsuarioJpaRepository;
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

    @Test
    @DisplayName("DemoDataSeeder precarga usuarios y scrims compatibles con reglas por juego")
    void demoDataSeederPrecargaDatosValidos() {
        assertTrue(usuarioRepository.existsByUsername("admin"));
        assertTrue(usuarioRepository.existsByUsername("echo"));
        assertEquals(8, usuarioRepository.count());

        assertTrue(scrimRepository.findAll().stream()
                .anyMatch(scrim -> "Valorant".equals(scrim.getJuego())
                        && "2v2".equals(scrim.getFormato())
                        && "CONFIRMADO".equals(scrim.getEstado())));

        assertTrue(scrimRepository.findAll().stream()
                .anyMatch(scrim -> "LoL".equals(scrim.getJuego())
                        && "2v2".equals(scrim.getFormato())
                        && scrim.getCuposTotales() == 4
                        && "BUSCANDO".equals(scrim.getEstado())));
    }
}
