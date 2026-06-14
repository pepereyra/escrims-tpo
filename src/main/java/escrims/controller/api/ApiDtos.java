package escrims.controller.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record CrearUsuarioRequest(String username,
                                      String email,
                                      String passwordHash,
                                      String region,
                                      String juego,
                                      int rango,
                                      int latencia,
                                      boolean verificarEmail) {
    }

    public record UsuarioResponse(UUID id,
                                  String username,
                                  String email,
                                  String region,
                                  int latenciaPromedio,
                                  boolean verificado) {
    }

    public record CrearScrimRequest(String juego,
                                    String formato,
                                    String region,
                                    int rangoMin,
                                    int rangoMax,
                                    int latenciaMax,
                                    LocalDateTime fechaHora,
                                    int duracionMinutos,
                                    int cuposTotales) {
    }

    public record PostulacionRequest(String username, String rol) {
    }

    public record UsuarioOperacionRequest(String username) {
    }

    public record NotificacionesRequest(List<String> usernames) {
    }

    public record SchedulerRequest(LocalDateTime ahora) {
    }

    public record EstadisticaInput(String username, int kills, int deaths, int assists) {
    }

    public record RegistrarEstadisticasRequest(List<EstadisticaInput> resultados) {
    }

    public record EstadisticaResponse(String username,
                                      int kills,
                                      int deaths,
                                      int assists,
                                      double kda,
                                      boolean mvp) {
    }

    public record ScrimResponse(UUID id,
                                String juego,
                                String formato,
                                String region,
                                String estado,
                                int cuposTotales,
                                int cuposDisponibles,
                                LocalDateTime fechaHora) {
    }

    public record SchedulerResponse(int iniciados) {
    }

    public record ErrorResponse(String error) {
    }
}
