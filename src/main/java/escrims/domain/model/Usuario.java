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
    private int strikes;
    private LocalDateTime cooldownHasta;
    private boolean verificado;

    public Usuario(String username, String email, String passwordHash, String region) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.region = region;
        this.rangoPorJuego = new HashMap<>();
        this.rolesPreferidos = new ArrayList<>();
        this.latenciaPromedio = 0;
        this.disponibilidad = "";
        this.strikes = 0;
        this.cooldownHasta = null;
        this.verificado = false;
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
        this.disponibilidad = disponibilidad;
    }

    public int getStrikes() {
        return strikes;
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
