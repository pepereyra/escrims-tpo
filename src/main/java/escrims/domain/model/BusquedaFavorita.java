package escrims.domain.model;

import escrims.infra.events.ScrimCreadoEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class BusquedaFavorita {

    private final UUID id;
    private final Usuario usuario;
    private final String juego;
    private final String formato;
    private final String region;
    private final Integer rangoMin;
    private final Integer rangoMax;
    private final Integer latenciaMax;
    private final LocalDate fecha;
    private final LocalDateTime fechaCreacion;

    public BusquedaFavorita(Usuario usuario,
                            String juego,
                            String formato,
                            String region,
                            Integer rangoMin,
                            Integer rangoMax,
                            Integer latenciaMax,
                            LocalDate fecha) {
        this(UUID.randomUUID(), usuario, juego, formato, region, rangoMin, rangoMax, latenciaMax, fecha, LocalDateTime.now());
    }

    public BusquedaFavorita(UUID id,
                            Usuario usuario,
                            String juego,
                            String formato,
                            String region,
                            Integer rangoMin,
                            Integer rangoMax,
                            Integer latenciaMax,
                            LocalDate fecha,
                            LocalDateTime fechaCreacion) {
        this.id = id;
        this.usuario = usuario;
        this.juego = normalizar(juego);
        this.formato = normalizar(formato);
        this.region = normalizar(region);
        this.rangoMin = rangoMin;
        this.rangoMax = rangoMax;
        this.latenciaMax = latenciaMax;
        this.fecha = fecha;
        this.fechaCreacion = fechaCreacion;
    }

    public boolean coincideCon(ScrimCreadoEvent evento) {
        return coincide(juego, evento.getJuego())
                && coincide(formato, evento.getFormato())
                && coincide(region, evento.getRegion())
                && (rangoMin == null || evento.getRangoMin() >= rangoMin)
                && (rangoMax == null || evento.getRangoMax() <= rangoMax)
                && (latenciaMax == null || evento.getLatenciaMax() <= latenciaMax)
                && (fecha == null || evento.getFechaHora().toLocalDate().equals(fecha));
    }

    private boolean coincide(String filtro, String valor) {
        return filtro == null || filtro.isBlank() || valor.equalsIgnoreCase(filtro);
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getJuego() {
        return juego;
    }

    public String getFormato() {
        return formato;
    }

    public String getRegion() {
        return region;
    }

    public Integer getRangoMin() {
        return rangoMin;
    }

    public Integer getRangoMax() {
        return rangoMax;
    }

    public Integer getLatenciaMax() {
        return latenciaMax;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
