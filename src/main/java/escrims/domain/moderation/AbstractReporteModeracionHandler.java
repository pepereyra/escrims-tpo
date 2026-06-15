package escrims.domain.moderation;

import escrims.domain.model.ReporteConducta;

public abstract class AbstractReporteModeracionHandler implements ReporteModeracionHandler {

    private ReporteModeracionHandler siguiente;

    public AbstractReporteModeracionHandler linkWith(ReporteModeracionHandler siguiente) {
        this.siguiente = siguiente;
        return this;
    }

    protected ReporteResolution siguiente(ReporteConducta reporte) {
        if (siguiente == null) {
            return ReporteResolution.pendiente("MODERADOR_HUMANO");
        }
        return siguiente.resolver(reporte);
    }
}
