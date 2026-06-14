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

    public record RegisterRequest(String username,
                                  String email,
                                  String password,
                                  String region,
                                  String juego,
                                  int rango,
                                  int latencia,
                                  List<String> rolesPreferidos,
                                  String disponibilidad) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record AuthResponse(String token, UsuarioResponse usuario) {
    }

    public record ActualizarPerfilRequest(String region,
                                          String juegoPrincipal,
                                          Integer rango,
                                          Integer latencia,
                                          List<String> rolesPreferidos,
                                          String disponibilidad) {
    }

    public record UsuarioResponse(UUID id,
                                  String username,
                                  String email,
                                  String region,
                                  String juegoPrincipal,
                                  int rangoPrincipal,
                                  int latenciaPromedio,
                                  List<String> rolesPreferidos,
                                  String disponibilidad,
                                  boolean verificado,
                                  String rolSistema,
                                  int strikes,
                                  LocalDateTime cooldownHasta) {
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

    public record CambiarRolRequest(String username, String nuevoRol) {
    }

    public record IntercambiarRolesRequest(String usernameA, String usernameB) {
    }

    public record NotificacionesRequest(List<String> usernames) {
    }

    public record SchedulerRequest(LocalDateTime ahora) {
    }

    public record EstadisticaInput(String username, int kills, int deaths, int assists) {
    }

    public record RegistrarEstadisticasRequest(List<EstadisticaInput> resultados) {
    }

    public record FeedbackRequest(String autor, String destinatario, int rating, String comentario) {
    }

    public record FeedbackResponse(UUID id,
                                   UUID scrimId,
                                   String autor,
                                   String destinatario,
                                   int rating,
                                   String comentario,
                                   String estado,
                                   LocalDateTime fechaCreacion) {
    }

    public record ReporteConductaRequest(String reportante, String reportado, String motivo) {
    }

    public record ResolverReporteRequest(String sancion) {
    }

    public record ReporteConductaResponse(UUID id,
                                          UUID scrimId,
                                          String reportante,
                                          String reportado,
                                          String motivo,
                                          String estado,
                                          String sancion,
                                          int strikesReportado,
                                          LocalDateTime fechaCreacion) {
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
                                LocalDateTime fechaHora,
                                List<PostulacionResponse> postulaciones) {
    }

    public record PostulacionResponse(String username, String rol, String estado) {
    }

    public record SchedulerResponse(int iniciados) {
    }

    public record ErrorResponse(String error) {
    }
}
