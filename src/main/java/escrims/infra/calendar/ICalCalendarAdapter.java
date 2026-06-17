package escrims.infra.calendar;

import escrims.domain.state.ScrimContext;

import java.time.format.DateTimeFormatter;

public class ICalCalendarAdapter implements CalendarAdapter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    @Override
    public String generarEvento(ScrimContext scrim) {
        String inicio = scrim.getFechaHora().format(FORMATTER);
        String fin = scrim.getFechaHora().plusMinutes(scrim.getDuracionMinutos()).format(FORMATTER);

        return String.join("\n",
                "BEGIN:VCALENDAR",
                "VERSION:2.0",
                "PRODID:-//eScrims//Scrim Calendar//ES",
                "BEGIN:VEVENT",
                "UID:" + scrim.getId() + "@escrims",
                "DTSTAMP:" + inicio,
                "DTSTART:" + inicio,
                "DTEND:" + fin,
                "SUMMARY:eScrims " + scrim.getJuego() + " " + scrim.getFormato(),
                "DESCRIPTION:Modalidad " + scrim.getModalidad() + " en region " + scrim.getRegion(),
                "LOCATION:" + scrim.getRegion(),
                "END:VEVENT",
                "END:VCALENDAR"
        );
    }
}
