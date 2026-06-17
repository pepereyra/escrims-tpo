package escrims.domain.moderation;

public class ReporteResolution {

    private final String etapa;
    private final boolean resuelto;
    private final boolean aprobado;
    private final String sancion;

    private ReporteResolution(String etapa, boolean resuelto, boolean aprobado, String sancion) {
        this.etapa = etapa;
        this.resuelto = resuelto;
        this.aprobado = aprobado;
        this.sancion = sancion == null ? "" : sancion;
    }

    public static ReporteResolution pendiente(String etapa) {
        return new ReporteResolution(etapa, false, false, "");
    }

    public static ReporteResolution aprobado(String etapa, String sancion) {
        return new ReporteResolution(etapa, true, true, sancion);
    }

    public static ReporteResolution rechazado(String etapa) {
        return new ReporteResolution(etapa, true, false, "");
    }

    public String getEtapa() {
        return etapa;
    }

    public boolean isResuelto() {
        return resuelto;
    }

    public boolean isAprobado() {
        return aprobado;
    }

    public String getSancion() {
        return sancion;
    }
}
