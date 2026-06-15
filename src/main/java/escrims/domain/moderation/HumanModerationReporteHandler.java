package escrims.domain.moderation;

import escrims.domain.model.ReporteConducta;

public class HumanModerationReporteHandler implements ReporteModeracionHandler {

    @Override
    public ReporteResolution resolver(ReporteConducta reporte) {
        return ReporteResolution.pendiente("MODERADOR_HUMANO");
    }
}
