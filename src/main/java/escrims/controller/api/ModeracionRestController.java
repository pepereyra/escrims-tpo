package escrims.controller.api;

import escrims.domain.model.Feedback;
import escrims.domain.model.ReporteConducta;
import escrims.service.AuthService;
import escrims.service.ModeracionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ModeracionRestController {

    private final ModeracionService moderacionService;
    private final UsuarioApiRepository usuarios;
    private final AuthService authService;

    public ModeracionRestController(ModeracionService moderacionService,
                                    UsuarioApiRepository usuarios,
                                    AuthService authService) {
        this.moderacionService = moderacionService;
        this.usuarios = usuarios;
        this.authService = authService;
    }

    @PostMapping("/scrims/{scrimId}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.FeedbackResponse registrarFeedback(@PathVariable("scrimId") UUID scrimId,
                                                      @RequestBody ApiDtos.FeedbackRequest request) {
        return toResponse(moderacionService.registrarFeedback(
                scrimId,
                usuarios.buscar(request.autor()),
                usuarios.buscar(request.destinatario()),
                request.rating(),
                request.comentario()
        ));
    }

    @GetMapping("/scrims/{scrimId}/feedback")
    public List<ApiDtos.FeedbackResponse> listarFeedback(@PathVariable("scrimId") UUID scrimId) {
        return moderacionService.listarFeedback(scrimId).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/feedback/{feedbackId}/aprobar")
    public ApiDtos.FeedbackResponse aprobarFeedback(@PathVariable("feedbackId") UUID feedbackId,
                                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requerirRol(authorization, "MOD", "ADMIN");
        return toResponse(moderacionService.aprobarFeedback(feedbackId));
    }

    @PostMapping("/feedback/{feedbackId}/rechazar")
    public ApiDtos.FeedbackResponse rechazarFeedback(@PathVariable("feedbackId") UUID feedbackId,
                                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requerirRol(authorization, "MOD", "ADMIN");
        return toResponse(moderacionService.rechazarFeedback(feedbackId));
    }

    @PostMapping("/scrims/{scrimId}/reportes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.ReporteConductaResponse registrarReporte(@PathVariable("scrimId") UUID scrimId,
                                                            @RequestBody ApiDtos.ReporteConductaRequest request) {
        return toResponse(moderacionService.registrarReporte(
                scrimId,
                usuarios.buscar(request.reportante()),
                usuarios.buscar(request.reportado()),
                request.motivo()
        ));
    }

    @GetMapping("/scrims/{scrimId}/reportes")
    public List<ApiDtos.ReporteConductaResponse> listarReportes(@PathVariable("scrimId") UUID scrimId) {
        return moderacionService.listarReportes(scrimId).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/reportes/{reporteId}/aprobar")
    public ApiDtos.ReporteConductaResponse aprobarReporte(@PathVariable("reporteId") UUID reporteId,
                                                          @RequestHeader(value = "Authorization", required = false) String authorization,
                                                          @RequestBody(required = false) ApiDtos.ResolverReporteRequest request) {
        authService.requerirRol(authorization, "MOD", "ADMIN");
        String sancion = request == null ? "" : request.sancion();
        return toResponse(moderacionService.aprobarReporte(reporteId, sancion));
    }

    @PostMapping("/reportes/{reporteId}/rechazar")
    public ApiDtos.ReporteConductaResponse rechazarReporte(@PathVariable("reporteId") UUID reporteId,
                                                           @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requerirRol(authorization, "MOD", "ADMIN");
        return toResponse(moderacionService.rechazarReporte(reporteId));
    }

    private ApiDtos.FeedbackResponse toResponse(Feedback feedback) {
        return new ApiDtos.FeedbackResponse(
                feedback.getId(),
                feedback.getScrimId(),
                feedback.getAutor().getUsername(),
                feedback.getDestinatario().getUsername(),
                feedback.getRating(),
                feedback.getComentario(),
                feedback.getEstado().getNombre(),
                feedback.getFechaCreacion()
        );
    }

    private ApiDtos.ReporteConductaResponse toResponse(ReporteConducta reporte) {
        return new ApiDtos.ReporteConductaResponse(
                reporte.getId(),
                reporte.getScrimId(),
                reporte.getReportante().getUsername(),
                reporte.getReportado().getUsername(),
                reporte.getMotivo(),
                reporte.getEstado().getNombre(),
                reporte.getSancion(),
                reporte.getReportado().getStrikes(),
                reporte.getFechaCreacion()
        );
    }
}
