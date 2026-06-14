package escrims.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "scrims")
public class ScrimJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String juego;

    @Column(nullable = false)
    private String formato;

    @Column(nullable = false)
    private String region;

    private int rangoMin;
    private int rangoMax;
    private int latenciaMax;
    private LocalDateTime fechaHora;
    private int duracionMinutos;
    private int cuposTotales;

    @Column(nullable = false)
    private String estado;

    @OneToMany(mappedBy = "scrim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PostulacionJpaEntity> postulaciones = new ArrayList<>();

    @OneToMany(mappedBy = "scrim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ConfirmacionJpaEntity> confirmaciones = new ArrayList<>();

    @OneToMany(mappedBy = "scrim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EstadisticaJpaEntity> estadisticas = new ArrayList<>();

    protected ScrimJpaEntity() {
    }

    public ScrimJpaEntity(UUID id,
                          String juego,
                          String formato,
                          String region,
                          int rangoMin,
                          int rangoMax,
                          int latenciaMax,
                          LocalDateTime fechaHora,
                          int duracionMinutos,
                          int cuposTotales,
                          String estado) {
        this.id = id;
        actualizar(juego, formato, region, rangoMin, rangoMax, latenciaMax, fechaHora, duracionMinutos, cuposTotales, estado);
    }

    public void actualizar(String juego,
                           String formato,
                           String region,
                           int rangoMin,
                           int rangoMax,
                           int latenciaMax,
                           LocalDateTime fechaHora,
                           int duracionMinutos,
                           int cuposTotales,
                           String estado) {
        this.juego = juego;
        this.formato = formato;
        this.region = region;
        this.rangoMin = rangoMin;
        this.rangoMax = rangoMax;
        this.latenciaMax = latenciaMax;
        this.fechaHora = fechaHora;
        this.duracionMinutos = duracionMinutos;
        this.cuposTotales = cuposTotales;
        this.estado = estado;
    }

    public void reemplazarPostulaciones(List<PostulacionJpaEntity> nuevasPostulaciones) {
        postulaciones.clear();
        nuevasPostulaciones.forEach(this::addPostulacion);
    }

    public void reemplazarConfirmaciones(List<ConfirmacionJpaEntity> nuevasConfirmaciones) {
        confirmaciones.clear();
        nuevasConfirmaciones.forEach(this::addConfirmacion);
    }

    public void reemplazarEstadisticas(List<EstadisticaJpaEntity> nuevasEstadisticas) {
        estadisticas.clear();
        nuevasEstadisticas.forEach(this::addEstadistica);
    }

    private void addPostulacion(PostulacionJpaEntity postulacion) {
        postulacion.setScrim(this);
        postulaciones.add(postulacion);
    }

    private void addConfirmacion(ConfirmacionJpaEntity confirmacion) {
        confirmacion.setScrim(this);
        confirmaciones.add(confirmacion);
    }

    private void addEstadistica(EstadisticaJpaEntity estadistica) {
        estadistica.setScrim(this);
        estadisticas.add(estadistica);
    }

    public UUID getId() {
        return id;
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

    public int getRangoMin() {
        return rangoMin;
    }

    public int getRangoMax() {
        return rangoMax;
    }

    public int getLatenciaMax() {
        return latenciaMax;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public int getCuposTotales() {
        return cuposTotales;
    }

    public String getEstado() {
        return estado;
    }

    public List<PostulacionJpaEntity> getPostulaciones() {
        return postulaciones;
    }

    public List<ConfirmacionJpaEntity> getConfirmaciones() {
        return confirmaciones;
    }

    public List<EstadisticaJpaEntity> getEstadisticas() {
        return estadisticas;
    }
}
