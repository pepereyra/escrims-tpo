package escrims.controller.api;

import java.time.LocalDateTime;
import java.time.LocalDate;
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
                                    int cuposTotales,
                                    String modalidad) {
        public CrearScrimRequest(String juego,
                                 String formato,
                                 String region,
                                 int rangoMin,
                                 int rangoMax,
                                 int latenciaMax,
                                 LocalDateTime fechaHora,
                                 int duracionMinutos,
                                 int cuposTotales) {
            this(juego, formato, region, rangoMin, rangoMax, latenciaMax, fechaHora, duracionMinutos,
                    cuposTotales, "CASUAL");
        }

        public CrearScrimRequest {
            if (modalidad == null || modalidad.isBlank()) {
                modalidad = "CASUAL";
            }
        }
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

    public record RecordatoriosRequest(LocalDateTime ahora, int horasAntes) {
    }

    public record RecordatorioResponse(UUID scrimId,
                                       String username,
                                       int horasAntes,
                                       String canal,
                                       String ical,
                                       LocalDateTime fechaEnvio) {
    }

    public record IcalResponse(UUID scrimId, String contenido) {
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

    public record BusquedaFavoritaRequest(String juego,
                                          String formato,
                                          String region,
                                          Integer rangoMin,
                                          Integer rangoMax,
                                          Integer latenciaMax,
                                          LocalDate fecha) {
    }

    public record BusquedaFavoritaResponse(UUID id,
                                           String username,
                                           String juego,
                                           String formato,
                                           String region,
                                           Integer rangoMin,
                                           Integer rangoMax,
                                           Integer latenciaMax,
                                           LocalDate fecha,
                                           LocalDateTime fechaCreacion) {
    }

    public record AlertaBusquedaResponse(UUID id,
                                         UUID busquedaId,
                                         String username,
                                         UUID scrimId,
                                         String mensaje,
                                         LocalDateTime fechaCreacion) {
    }

    public record DashboardMeResponse(UsuarioResponse usuario,
                                      int misScrimsTotal,
                                      int scrimsPorConfirmar,
                                      int alertasTotal,
                                      int busquedasFavoritasTotal,
                                      List<MiScrimResponse> proximosScrims,
                                      List<AlertaBusquedaResponse> alertasRecientes) {
    }

    public record MisScrimsResponse(List<MiScrimResponse> scrims) {
    }

    public record MiScrimResponse(UUID id,
                                  String juego,
                                  String formato,
                                  String region,
                                  String estado,
                                  String modalidad,
                                  int cuposTotales,
                                  int cuposDisponibles,
                                  LocalDateTime fechaHora,
                                  String miRol,
                                  String miEstadoPostulacion,
                                  boolean confirmado,
                                  boolean puedeConfirmar) {
    }

    public record ParticipantesScrimResponse(UUID scrimId,
                                             String estado,
                                             int cuposTotales,
                                             int cuposDisponibles,
                                             List<EquipoResponse> equipos,
                                             List<ParticipanteScrimResponse> aceptados,
                                             List<ParticipanteScrimResponse> suplentes,
                                             List<ParticipanteScrimResponse> pendientes,
                                             List<ParticipanteScrimResponse> rechazados) {
    }

    public record ParticipanteScrimResponse(String username,
                                            String rol,
                                            String estadoPostulacion,
                                            boolean confirmado,
                                            LocalDateTime fechaConfirmacion,
                                            int rangoEnJuego,
                                            int latenciaPromedio) {
    }

    public record CatalogosResponse(List<JuegoCatalogoResponse> juegos,
                                    List<String> modalidades) {
    }

    public record JuegoCatalogoResponse(String juego,
                                        List<String> formatosPermitidos,
                                        List<String> rolesPermitidos) {
    }

    public record AuditLogResponse(UUID id,
                                   String actor,
                                   String accion,
                                   String entidadTipo,
                                   String entidadId,
                                   String detalle,
                                   LocalDateTime fecha) {
    }

    public record ReporteConductaResponse(UUID id,
                                          UUID scrimId,
                                          String reportante,
                                          String reportado,
                                          String motivo,
                                          String estado,
                                          String sancion,
                                          String etapaResolucion,
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
                                String modalidad,
                                int cuposDisponibles,
                                LocalDateTime fechaHora,
                                List<EquipoResponse> equipos,
                                List<PostulacionResponse> postulaciones) {
    }

    public record EquipoResponse(String lado, List<ParticipanteScrimResponse> jugadores) {
    }

    public record PostulacionResponse(String username, String rol, String estado) {
    }

    public record SchedulerResponse(int iniciados) {
    }

    public record ErrorResponse(String error) {
    }
}
