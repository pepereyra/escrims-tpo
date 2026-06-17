package escrims.domain.moderation;

import escrims.domain.model.ReporteConducta;

public class AutoResolveReporteHandler extends AbstractReporteModeracionHandler {

    @Override
    public ReporteResolution resolver(ReporteConducta reporte) {
        String motivo = reporte.getMotivo().toLowerCase();
        if (motivo.contains("auto-no-show") || motivo.contains("abandono confirmado")) {
            return ReporteResolution.aprobado("AUTO_RESOLVER", "STRIKE_AUTO_NO_SHOW");
        }
        if (motivo.contains("reporte falso automatico")) {
            return ReporteResolution.rechazado("AUTO_RESOLVER");
        }
        return siguiente(reporte);
    }
}
