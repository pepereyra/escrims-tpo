package escrims.infra.persistence;

import escrims.domain.model.Usuario;
import escrims.domain.model.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String region;

    private String juegoPrincipal;
    private int rangoPrincipal;
    private String rolesPreferidos;
    private String disponibilidad;
    private int latenciaPromedio;
    private boolean verificado;
    private String rolSistema;
    private int strikes;
    private LocalDateTime cooldownHasta;

    protected UsuarioJpaEntity() {
    }

    public UsuarioJpaEntity(UUID id,
                            String username,
                            String email,
                            String passwordHash,
                            String region,
                            String juegoPrincipal,
                            int rangoPrincipal,
                            String rolesPreferidos,
                            String disponibilidad,
                            int latenciaPromedio,
                            boolean verificado,
                            String rolSistema,
                            int strikes,
                            LocalDateTime cooldownHasta) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.region = region;
        this.juegoPrincipal = juegoPrincipal;
        this.rangoPrincipal = rangoPrincipal;
        this.rolesPreferidos = rolesPreferidos;
        this.disponibilidad = disponibilidad;
        this.latenciaPromedio = latenciaPromedio;
        this.verificado = verificado;
        this.rolSistema = rolSistema;
        this.strikes = strikes;
        this.cooldownHasta = cooldownHasta;
    }

    public static UsuarioJpaEntity fromDomain(Usuario usuario) {
        String juego = usuario.getRangoPorJuego().keySet().stream().findFirst().orElse("");
        int rango = juego.isBlank() ? 0 : usuario.getRangoEnJuego(juego);

        return new UsuarioJpaEntity(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getRegion(),
                juego,
                rango,
                usuario.getRolesPreferidos().stream()
                        .map(Rol::getNombre)
                        .reduce((a, b) -> a + "," + b)
                        .orElse(""),
                usuario.getDisponibilidad(),
                usuario.getLatenciaPromedio(),
                usuario.isVerificado(),
                usuario.getRolSistema(),
                usuario.getStrikes(),
                usuario.getCooldownHasta()
        );
    }

    public Usuario toDomain() {
        Usuario usuario = new Usuario(id, username, email, passwordHash, region, verificado, strikes, cooldownHasta, rolSistema);
        usuario.setLatenciaPromedio(latenciaPromedio);
        usuario.setDisponibilidad(disponibilidad);

        Map<String, Integer> rangos = new HashMap<>();
        if (juegoPrincipal != null && !juegoPrincipal.isBlank()) {
            rangos.put(juegoPrincipal, rangoPrincipal);
        }
        usuario.setRangoPorJuego(rangos);
        if (rolesPreferidos != null && !rolesPreferidos.isBlank()) {
            usuario.setRolesPreferidos(java.util.Arrays.stream(rolesPreferidos.split(","))
                    .filter(rol -> !rol.isBlank())
                    .map(Rol::new)
                    .toList());
        }

        return usuario;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getStrikes() {
        return strikes;
    }

    public LocalDateTime getCooldownHasta() {
        return cooldownHasta;
    }
}
