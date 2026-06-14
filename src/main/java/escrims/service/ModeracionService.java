package escrims.service;

import escrims.domain.model.Feedback;
import escrims.domain.model.ReporteConducta;
import escrims.domain.model.Usuario;
import escrims.domain.state.ScrimContext;

import java.util.List;
import java.util.UUID;

public class ModeracionService {

    private final ScrimService scrimService;
    private final FeedbackRepository feedbackRepository;
    private final ReporteConductaRepository reporteRepository;

    public ModeracionService(ScrimService scrimService,
                             FeedbackRepository feedbackRepository,
                             ReporteConductaRepository reporteRepository) {
        this.scrimService = scrimService;
        this.feedbackRepository = feedbackRepository;
        this.reporteRepository = reporteRepository;
    }

    public Feedback registrarFeedback(UUID scrimId,
                                      Usuario autor,
                                      Usuario destinatario,
                                      int rating,
                                      String comentario) {
        ScrimContext scrim = scrimService.getScrim(scrimId);

        if (!scrim.getState().getNombre().equals("FINALIZADO")) {
            throw new IllegalStateException("El feedback solo se puede cargar en scrims finalizados.");
        }

        validarParticipante(scrim, autor);
        validarParticipante(scrim, destinatario);

        return feedbackRepository.save(new Feedback(scrimId, autor, destinatario, rating, comentario));
    }

    public Feedback aprobarFeedback(UUID feedbackId) {
        Feedback feedback = buscarFeedback(feedbackId);
        validarPendiente(feedback.getEstado().getNombre());
        feedback.aprobar();
        return feedbackRepository.save(feedback);
    }

    public Feedback rechazarFeedback(UUID feedbackId) {
        Feedback feedback = buscarFeedback(feedbackId);
        validarPendiente(feedback.getEstado().getNombre());
        feedback.rechazar();
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> listarFeedback(UUID scrimId) {
        return feedbackRepository.findByScrimId(scrimId).stream().toList();
    }

    public ReporteConducta registrarReporte(UUID scrimId,
                                            Usuario reportante,
                                            Usuario reportado,
                                            String motivo) {
        ScrimContext scrim = scrimService.getScrim(scrimId);
        validarParticipante(scrim, reportante);
        validarParticipante(scrim, reportado);

        return reporteRepository.save(new ReporteConducta(scrimId, reportante, reportado, motivo));
    }

    public ReporteConducta aprobarReporte(UUID reporteId, String sancion) {
        ReporteConducta reporte = buscarReporte(reporteId);
        validarPendiente(reporte.getEstado().getNombre());
        reporte.aprobar(sancion);
        return reporteRepository.save(reporte);
    }

    public ReporteConducta rechazarReporte(UUID reporteId) {
        ReporteConducta reporte = buscarReporte(reporteId);
        validarPendiente(reporte.getEstado().getNombre());
        reporte.rechazar();
        return reporteRepository.save(reporte);
    }

    public List<ReporteConducta> listarReportes(UUID scrimId) {
        return reporteRepository.findByScrimId(scrimId).stream().toList();
    }

    private Feedback buscarFeedback(UUID feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback no encontrado: " + feedbackId));
    }

    private ReporteConducta buscarReporte(UUID reporteId) {
        return reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + reporteId));
    }

    private void validarParticipante(ScrimContext scrim, Usuario usuario) {
        boolean participa = scrim.getPostulaciones().stream()
                .anyMatch(postulacion -> postulacion.getUsuario().getId().equals(usuario.getId()));

        if (!participa) {
            throw new IllegalArgumentException(
                    "El usuario " + usuario.getUsername() + " no participo del scrim " + scrim.getId() + "."
            );
        }
    }

    private void validarPendiente(String estado) {
        if (!"PENDIENTE".equals(estado)) {
            throw new IllegalStateException("La accion de moderacion ya fue resuelta.");
        }
    }
}
