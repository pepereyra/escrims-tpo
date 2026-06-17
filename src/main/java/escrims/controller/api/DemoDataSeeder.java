package escrims.controller.api;

import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;
import escrims.service.PasswordHasher;
import escrims.service.ScrimService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "12345678";

    private final boolean enabled;
    private final UsuarioApiRepository usuarios;
    private final PasswordHasher passwordHasher;
    private final ScrimService scrimService;

    public DemoDataSeeder(@Value("${app.demo-data.enabled:false}") boolean enabled,
                          UsuarioApiRepository usuarios,
                          PasswordHasher passwordHasher,
                          ScrimService scrimService) {
        this.enabled = enabled;
        this.usuarios = usuarios;
        this.passwordHasher = passwordHasher;
        this.scrimService = scrimService;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        List<Usuario> demoUsers = List.of(
                seedUsuario("admin", "admin@escrims.local", "ADMIN", "Valorant", 1900, "SA", 22, List.of("DUELIST", "IGL")),
                seedUsuario("mod", "mod@escrims.local", "MOD", "Valorant", 1750, "SA", 28, List.of("SUPPORT", "FLEX")),
                seedUsuario("alpha", "alpha@escrims.local", "USER", "Valorant", 1500, "SA", 30, List.of("DUELIST")),
                seedUsuario("bravo", "bravo@escrims.local", "USER", "Valorant", 1580, "SA", 25, List.of("SUPPORT")),
                seedUsuario("charlie", "charlie@escrims.local", "USER", "Valorant", 1460, "SA", 35, List.of("DUELIST")),
                seedUsuario("delta", "delta@escrims.local", "USER", "Valorant", 1540, "SA", 32, List.of("SUPPORT")),
                seedUsuario("echo", "echo@escrims.local", "USER", "LoL", 1620, "BR", 45, List.of("JUNGLA")),
                seedUsuario("foxtrot", "foxtrot@escrims.local", "USER", "LoL", 1680, "BR", 42, List.of("MID"))
        );

        if (scrimService.getScrims().isEmpty()) {
            seedScrims(demoUsers);
        }

        System.out.println("[DemoDataSeeder] Datos demo listos. Usuarios: admin/mod/alpha/bravo/charlie/delta/echo/foxtrot | password: " + DEMO_PASSWORD);
    }

    private Usuario seedUsuario(String username,
                                String email,
                                String rolSistema,
                                String juego,
                                int rango,
                                String region,
                                int latencia,
                                List<String> roles) {
        if (usuarios.existeUsername(username)) {
            return usuarios.buscar(username);
        }

        Usuario usuario = new Usuario(username, email, passwordHasher.hash(DEMO_PASSWORD), region);
        usuario.verificarEmail();
        usuario.setRolSistema(rolSistema);
        usuario.setRangoPorJuego(Map.of(juego, rango));
        usuario.setLatenciaPromedio(latencia);
        usuario.setDisponibilidad("Lunes a viernes 20-24");
        usuario.setRolesPreferidos(roles.stream().map(Rol::new).toList());
        return usuarios.guardar(usuario);
    }

    private void seedScrims(List<Usuario> demoUsers) {
        Usuario alpha = byUsername(demoUsers, "alpha");
        Usuario bravo = byUsername(demoUsers, "bravo");
        Usuario charlie = byUsername(demoUsers, "charlie");
        Usuario delta = byUsername(demoUsers, "delta");

        ScrimContext confirmado = scrimService.crearScrim(
                "Valorant",
                "2v2",
                "SA",
                1400,
                1700,
                80,
                LocalDateTime.now().plusHours(2).withNano(0),
                30,
                4,
                "PRACTICA"
        );
        scrimService.postular(confirmado.getId(), alpha, Rol.DUELIST);
        scrimService.postular(confirmado.getId(), bravo, Rol.SUPPORT);
        scrimService.postular(confirmado.getId(), charlie, Rol.DUELIST);
        scrimService.postular(confirmado.getId(), delta, Rol.SUPPORT);
        scrimService.confirmar(confirmado.getId(), alpha);
        scrimService.confirmar(confirmado.getId(), bravo);
        scrimService.confirmar(confirmado.getId(), charlie);
        scrimService.confirmar(confirmado.getId(), delta);

        scrimService.crearScrim(
                "LoL",
                "2v2",
                "BR",
                1500,
                1800,
                90,
                LocalDateTime.now().plusDays(1).withNano(0),
                45,
                4,
                "CASUAL"
        );
    }

    private Usuario byUsername(List<Usuario> usuarios, String username) {
        return usuarios.stream()
                .filter(usuario -> username.equals(usuario.getUsername()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Usuario demo no encontrado: " + username));
    }
}
