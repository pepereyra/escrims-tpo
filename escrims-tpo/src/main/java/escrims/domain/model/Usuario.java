package escrims.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Usuario {

    private final UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private Map<String, Integer> rangoPorJuego;
    private List<Rol> rolesPreferidos;
    private String region;
    private int latenciaPromedio;
    private String disponibilidad;
    private String rolSistema;
    private int strikes;
    private LocalDateTime cooldownHasta;
    private boolean verificado;

    public Usuario(String username, String email, String passwordHash, String region) {
        this(UUID.randomUUID(), username, email, passwordHash, region);
    }

    public Usuario(UUID id, String username, String email, String passwordHash, String region) {
        this(id, username, email, passwordHash, region, false);
    }

    public Usuario(UUID id,
                   String username,
                   String email,
                   String passwordHash,
                   String region,
                   boolean verificado) {
        this(id, username, email, passwordHash, region, verificado, 0, null);
    }

    public Usuario(UUID id,
                   String username,
                   String email,
                   String passwordHash,
                   String region,
                   boolean verificado,
                   int strikes,
                   LocalDateTime cooldownHasta) {
        this(id, username, email, passwordHash, region, verificado, strikes, cooldownHasta, "USER");
    }

    public Usuario(UUID id,
                   String username,
                   String email,
                   String passwordHash,
                   String region,
                   boolean verificado,
                   int strikes,
                   LocalDateTime cooldownHasta,
                   String rolSistema) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.region = region;
        this.rangoPorJuego = new HashMap<>();
        this.rolesPreferidos = new ArrayList<>();
        this.latenciaPromedio = 0;
        this.disponibilidad = "";
        this.rolSistema = rolSistema == null || rolSistema.isBlank() ? "USER" : rolSistema;
        this.strikes = strikes;
        this.cooldownHasta = cooldownHasta;
        this.verificado = verificado;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Map<String, Integer> getRangoPorJuego() {
        return rangoPorJuego;
    }

    public void setRangoPorJuego(Map<String, Integer> rangoPorJuego) {
        this.rangoPorJuego = rangoPorJuego;
    }

    public List<Rol> getRolesPreferidos() {
        return rolesPreferidos;
    }

    public void setRolesPreferidos(List<Rol> rolesPreferidos) {
        this.rolesPreferidos = rolesPreferidos;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getLatenciaPromedio() {
        return latenciaPromedio;
    }

    public void setLatenciaPromedio(int latenciaPromedio) {
        this.latenciaPromedio = latenciaPromedio;
    }

    public String getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(String disponibilidad) {
        this.disponibilidad = disponibilidad == null ? "" : disponibilidad;
    }

    public String getRolSistema() {
        return rolSistema;
    }

    public void setRolSistema(String rolSistema) {
        this.rolSistema = rolSistema == null || rolSistema.isBlank() ? "USER" : rolSistema;
    }

    public int getStrikes() {
        return strikes;
    }

    public LocalDateTime getCooldownHasta() {
        return cooldownHasta;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public boolean isCooldownActivo() {
        return cooldownHasta != null && LocalDateTime.now().isBefore(cooldownHasta);
    }

    public void agregarStrike() {
        this.strikes++;
        if (this.strikes >= 3) {
            activarCooldown(24);
        }
    }

    public void activarCooldown(int horas) {
        this.cooldownHasta = LocalDateTime.now().plusHours(horas);
        System.out.println("[Usuario] " + username + " tiene cooldown hasta: " + cooldownHasta);
    }

    public void verificarEmail() {
        this.verificado = true;
        System.out.println("[Usuario] Email verificado para: " + username);
    }

    public int getRangoEnJuego(String juego) {
        return rangoPorJuego.getOrDefault(juego, 0);
    }

    @Override
    public String toString() {
        return "Usuario{username='" + username + "', region='" + region + "', verificado=" + verificado + "}";
    }
}
