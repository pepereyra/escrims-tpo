package escrims.controller.api;

import escrims.domain.model.Rol;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;
import escrims.infra.persistence.SpringDataBusquedaFavoritaJpaRepository;
import escrims.infra.persistence.SpringDataFeedbackJpaRepository;
import escrims.infra.persistence.SpringDataReporteConductaJpaRepository;
import escrims.service.PasswordHasher;
import escrims.service.BusquedaFavoritaService;
import escrims.service.ModeracionService;
import escrims.service.ScrimService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "12345678";
    private static final int TARGET_USERS = 120;
    private static final int TARGET_SCRIMS = 120;
    private static final int TARGET_BUSQUEDAS = 120;
    private static final int TARGET_FEEDBACK = 120;
    private static final int TARGET_REPORTES = 120;

    private final boolean enabled;
    private final UsuarioApiRepository usuarios;
    private final PasswordHasher passwordHasher;
    private final ScrimService scrimService;
    private final BusquedaFavoritaService busquedaFavoritaService;
    private final ModeracionService moderacionService;
    private final SpringDataBusquedaFavoritaJpaRepository busquedaRepository;
    private final SpringDataFeedbackJpaRepository feedbackRepository;
    private final SpringDataReporteConductaJpaRepository reporteRepository;

    public DemoDataSeeder(@Value("${app.demo-data.enabled:false}") boolean enabled,
                          UsuarioApiRepository usuarios,
                          PasswordHasher passwordHasher,
                          ScrimService scrimService,
                          BusquedaFavoritaService busquedaFavoritaService,
                          ModeracionService moderacionService,
                          SpringDataBusquedaFavoritaJpaRepository busquedaRepository,
                          SpringDataFeedbackJpaRepository feedbackRepository,
                          SpringDataReporteConductaJpaRepository reporteRepository) {
        this.enabled = enabled;
        this.usuarios = usuarios;
        this.passwordHasher = passwordHasher;
        this.scrimService = scrimService;
        this.busquedaFavoritaService = busquedaFavoritaService;
        this.moderacionService = moderacionService;
        this.busquedaRepository = busquedaRepository;
        this.feedbackRepository = feedbackRepository;
        this.reporteRepository = reporteRepository;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        seedCoreUsers();
        seedGeneratedUsers();

        List<Usuario> demoUsers = usuarios.listar().stream()
                .sorted(Comparator.comparing(Usuario::getUsername))
                .toList();

        seedBusquedas(demoUsers);
        seedScrims(demoUsers);
        seedModeracion();

        System.out.println("[DemoDataSeeder] Datos demo listos. Usuarios >= " + TARGET_USERS
                + ", scrims >= " + TARGET_SCRIMS
                + ", busquedas >= " + TARGET_BUSQUEDAS
                + ", feedback >= " + TARGET_FEEDBACK
                + ", reportes >= " + TARGET_REPORTES
                + " | password usuarios demo: " + DEMO_PASSWORD);
    }

    private void seedCoreUsers() {
        List.of(
                seedUsuario("admin", "admin@escrims.local", "ADMIN", "Valorant", 1900, "SA", 22, List.of("DUELIST", "IGL")),
                seedUsuario("mod", "mod@escrims.local", "MOD", "Valorant", 1750, "SA", 28, List.of("SUPPORT", "FLEX")),
                seedUsuario("alpha", "alpha@escrims.local", "USER", "Valorant", 1500, "SA", 30, List.of("DUELIST")),
                seedUsuario("bravo", "bravo@escrims.local", "USER", "Valorant", 1580, "SA", 25, List.of("SUPPORT")),
                seedUsuario("charlie", "charlie@escrims.local", "USER", "Valorant", 1460, "SA", 35, List.of("DUELIST")),
                seedUsuario("delta", "delta@escrims.local", "USER", "Valorant", 1540, "SA", 32, List.of("SUPPORT")),
                seedUsuario("echo", "echo@escrims.local", "USER", "LoL", 1620, "BR", 45, List.of("JUNGLA")),
                seedUsuario("foxtrot", "foxtrot@escrims.local", "USER", "LoL", 1680, "BR", 42, List.of("MID"))
        );
    }

    private void seedGeneratedUsers() {
        int index = 1;
        while (usuarios.listar().size() < TARGET_USERS) {
            String username = "demo%03d".formatted(index);
            if (!usuarios.existeUsername(username)) {
                String juego = juegoPara(index);
                seedUsuario(
                        username,
                        username + "@escrims.local",
                        "USER",
                        juego,
                        rangoPara(index),
                        regionPara(index),
                        latenciaPara(index),
                        rolesPara(juego, index)
                );
            }
            index++;
        }
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

    private void seedBusquedas(List<Usuario> demoUsers) {
        int index = 1;
        while (busquedaRepository.count() < TARGET_BUSQUEDAS) {
            Usuario usuario = demoUsers.get(index % demoUsers.size());
            String juego = juegoPara(index);
            busquedaFavoritaService.guardar(
                    usuario,
                    juego,
                    "2v2",
                    regionPara(index),
                    1000,
                    2200,
                    120,
                    index % 3 == 0 ? LocalDate.now().plusDays(index % 14 + 1) : null
            );
            index++;
        }
    }

    private void seedScrims(List<Usuario> demoUsers) {
        int index = 1;
        while (scrimService.getScrims().size() < TARGET_SCRIMS) {
            String juego = juegoPara(index);
            List<Usuario> participantes = participantesPara(demoUsers, juego, index, 4);
            ScrimContext scrim = scrimService.crearScrim(
                    juego,
                    "2v2",
                    regionPara(index),
                    1000,
                    2200,
                    120,
                    LocalDateTime.now().plusHours(2L + index).withNano(0),
                    30 + (index % 4) * 15,
                    4,
                    modalidadPara(index)
            );

            int estadoDemo = index % 4;
            if (estadoDemo >= 1) {
                postularParticipantes(scrim, participantes, juego);
            }
            if (estadoDemo >= 2) {
                confirmarParticipantes(scrim, participantes);
            }
            if (estadoDemo == 3) {
                scrimService.iniciar(scrim.getId());
                scrimService.finalizar(scrim.getId());
            }
            index++;
        }
    }

    private void seedModeracion() {
        List<ScrimContext> finalizados = scrimService.getScrims().values().stream()
                .filter(scrim -> "FINALIZADO".equals(scrim.getState().getNombre()))
                .toList();

        int index = 0;
        while (feedbackRepository.count() < TARGET_FEEDBACK && !finalizados.isEmpty()) {
            ScrimContext scrim = finalizados.get(index % finalizados.size());
            List<Usuario> participantes = participantesAceptados(scrim);
            Usuario autor = participantes.get(index % participantes.size());
            Usuario destinatario = participantes.get((index + 1) % participantes.size());
            moderacionService.registrarFeedback(
                    scrim.getId(),
                    autor,
                    destinatario,
                    3 + (index % 3),
                    "Feedback demo " + (index + 1) + ": buen ritmo y comunicacion."
            );
            index++;
        }

        index = 0;
        while (reporteRepository.count() < TARGET_REPORTES && !finalizados.isEmpty()) {
            ScrimContext scrim = finalizados.get(index % finalizados.size());
            List<Usuario> participantes = participantesAceptados(scrim);
            Usuario reportante = participantes.get(index % participantes.size());
            Usuario reportado = participantes.get((index + 2) % participantes.size());
            moderacionService.registrarReporte(
                    scrim.getId(),
                    reportante,
                    reportado,
                    "Reporte demo " + (index + 1) + ": revisar conducta en partida."
            );
            index++;
        }
    }

    private void postularParticipantes(ScrimContext scrim, List<Usuario> participantes, String juego) {
        for (int i = 0; i < participantes.size(); i++) {
            scrimService.postular(scrim.getId(), participantes.get(i), new Rol(rolesPara(juego, i).get(0)));
        }
    }

    private void confirmarParticipantes(ScrimContext scrim, List<Usuario> participantes) {
        participantes.forEach(usuario -> scrimService.confirmar(scrim.getId(), usuario));
    }

    private List<Usuario> participantesPara(List<Usuario> usuarios, String juego, int offset, int cantidad) {
        List<Usuario> compatibles = usuarios.stream()
                .filter(usuario -> usuario.getRangoEnJuego(juego) >= 1000)
                .toList();
        if (compatibles.size() < cantidad) {
            throw new IllegalStateException("No hay suficientes usuarios demo para " + juego);
        }

        List<Usuario> seleccionados = new ArrayList<>();
        int start = Math.floorMod(offset, compatibles.size());
        IntStream.range(0, cantidad)
                .map(i -> (start + i) % compatibles.size())
                .mapToObj(compatibles::get)
                .forEach(seleccionados::add);
        return seleccionados;
    }

    private List<Usuario> participantesAceptados(ScrimContext scrim) {
        return scrim.getPostulaciones().stream()
                .filter(postulacion -> postulacion.estaAceptada())
                .map(postulacion -> postulacion.getUsuario())
                .toList();
    }

    private String juegoPara(int index) {
        return switch (index % 3) {
            case 1 -> "Valorant";
            case 2 -> "LoL";
            default -> "CS2";
        };
    }

    private String regionPara(int index) {
        return switch (index % 4) {
            case 1 -> "SA";
            case 2 -> "BR";
            case 3 -> "NA";
            default -> "EU";
        };
    }

    private int rangoPara(int index) {
        return 1200 + (index % 9) * 100;
    }

    private int latenciaPara(int index) {
        return 25 + (index % 8) * 8;
    }

    private String modalidadPara(int index) {
        return switch (index % 3) {
            case 1 -> "RANKED_LIKE";
            case 2 -> "PRACTICA";
            default -> "CASUAL";
        };
    }

    private List<String> rolesPara(String juego, int index) {
        List<String> roles = switch (juego) {
            case "LoL" -> List.of("TOP", "JUNGLA", "MID", "ADC", "SUPPORT");
            case "CS2" -> List.of("IGL", "ENTRY", "AWP", "SUPPORT", "LURKER");
            default -> List.of("DUELIST", "CONTROLLER", "SENTINEL", "INITIATOR", "SUPPORT");
        };
        return List.of(roles.get(Math.floorMod(index, roles.size())));
    }
}
