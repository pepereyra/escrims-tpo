package escrims.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class RecordatorioScrim {

    private final UUID scrimId;
    private final String username;
    private final int horasAntes;
    private final String canal;
    private final String ical;
    private final LocalDateTime fechaEnvio;

    public RecordatorioScrim(UUID scrimId,
                             String username,
                             int horasAntes,
                             String canal,
                             String ical,
                             LocalDateTime fechaEnvio) {
        this.scrimId = scrimId;
        this.username = username;
        this.horasAntes = horasAntes;
        this.canal = canal;
        this.ical = ical;
        this.fechaEnvio = fechaEnvio;
    }

    public UUID getScrimId() {
        return scrimId;
    }

    public String getUsername() {
        return username;
    }

    public int getHorasAntes() {
        return horasAntes;
    }

    public String getCanal() {
        return canal;
    }

    public String getIcal() {
        return ical;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }
}
