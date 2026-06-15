package escrims.domain.moderation;

import escrims.domain.model.ReporteConducta;

public class BotModerationReporteHandler extends AbstractReporteModeracionHandler {

    @Override
    public ReporteResolution resolver(ReporteConducta reporte) {
        String motivo = reporte.getMotivo().toLowerCase();
        if (motivo.contains("insulto") || motivo.contains("tox") || motivo.contains("grief")) {
            return ReporteResolution.pendiente("BOT_ESCALO_A_MODERADOR");
        }
        return siguiente(reporte);
    }
}
