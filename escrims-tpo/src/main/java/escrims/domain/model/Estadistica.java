package escrims.domain.model;

import java.util.UUID;

public class Estadistica {

    private final UUID id;
    private final Usuario usuario;
    private boolean mvp;
    private int kills;
    private int deaths;
    private int assists;
    private String observaciones;

    public Estadistica(Usuario usuario) {
        this(UUID.randomUUID(), usuario, false, 0, 0, 0, "");
    }

    public Estadistica(UUID id,
                       Usuario usuario,
                       boolean mvp,
                       int kills,
                       int deaths,
                       int assists,
                       String observaciones) {
        this.id = id;
        this.usuario = usuario;
        this.mvp = mvp;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.observaciones = observaciones == null ? "" : observaciones;
    }

    public UUID getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public boolean isMvp() { return mvp; }
    public void setMvp(boolean mvp) { this.mvp = mvp; }
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public int getAssists() { return assists; }
    public void setAssists(int assists) { this.assists = assists; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public double getKda() {
        return deaths == 0 ? (kills + assists) : (double)(kills + assists) / deaths;
    }

    @Override
    public String toString() {
        return "Estadistica{usuario='" + usuario.getUsername() + "', KDA=" + String.format("%.2f", getKda()) + ", mvp=" + mvp + "}";
    }
}
