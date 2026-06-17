package escrims.service;

import escrims.domain.state.ScrimContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de aplicación que simula el scheduler del backend.
 *
 * La consigna pide que un scrim confirmado pase a EN_JUEGO al llegar su fecha
 * y hora. En esta versión no se crea un thread ni se depende de cron: se expone
 * un método invocable desde tests, demo o una futura API REST.
 */
public class ScrimSchedulerService {

    private final ScrimService scrimService;

    public ScrimSchedulerService(ScrimService scrimService) {
        this.scrimService = scrimService;
    }

    public int procesarScrimsProgramados() {
        return procesarScrimsProgramados(LocalDateTime.now());
    }

    public int procesarScrimsProgramados(LocalDateTime ahora) {
        if (ahora == null) {
            throw new IllegalArgumentException("La fecha de procesamiento no puede ser null.");
        }

        List<ScrimContext> snapshot = new ArrayList<>(scrimService.getScrims().values());
        int iniciados = 0;

        for (ScrimContext scrim : snapshot) {
            if (debeIniciar(scrim, ahora)) {
                scrimService.iniciar(scrim.getId());
                iniciados++;
            }
        }

        System.out.println("[ScrimSchedulerService] Scrims iniciados automáticamente: " + iniciados);
        return iniciados;
    }

    private boolean debeIniciar(ScrimContext scrim, LocalDateTime ahora) {
        return scrim.getState().getNombre().equals("CONFIRMADO")
                && !scrim.getFechaHora().isAfter(ahora);
    }
}
