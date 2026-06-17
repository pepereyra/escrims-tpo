package escrims.domain.moderation;

import escrims.domain.model.ReporteConducta;

public interface ReporteModeracionHandler {

    ReporteResolution resolver(ReporteConducta reporte);
}
