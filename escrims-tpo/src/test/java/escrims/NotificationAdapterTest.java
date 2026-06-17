package escrims;

import escrims.domain.model.Notificacion;
import escrims.domain.model.Usuario;
import escrims.infra.notification.NotificadorFactory;
import escrims.infra.notification.NotificadorStrategy;
import escrims.infra.notification.ProdNotificadorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationAdapterTest {

    private final NotificadorFactory factory = new ProdNotificadorFactory();

    @Test
    @DisplayName("ProdFactory crea adapter de Email compatible con NotificadorStrategy")
    void prodFactoryCreaAdapterEmail() {
        NotificadorStrategy adapter = factory.crearEmailNotificador();
        Notificacion notificacion = new Notificacion("CONFIRMADO", adapter.getCanal(), "Scrim confirmado");

        adapter.enviar(crearUsuario(), notificacion);

        assertEquals("EMAIL", adapter.getCanal());
        assertEquals("ENVIADA", notificacion.getEstado().getNombre());
    }

    @Test
    @DisplayName("ProdFactory crea adapter de Push compatible con NotificadorStrategy")
    void prodFactoryCreaAdapterPush() {
        NotificadorStrategy adapter = factory.crearPushNotificador();
        Notificacion notificacion = new Notificacion("EN_JUEGO", adapter.getCanal(), "Scrim iniciado");

        adapter.enviar(crearUsuario(), notificacion);

        assertEquals("PUSH", adapter.getCanal());
        assertEquals("ENVIADA", notificacion.getEstado().getNombre());
    }

    @Test
    @DisplayName("ProdFactory crea adapter de Discord compatible con NotificadorStrategy")
    void prodFactoryCreaAdapterDiscord() {
        NotificadorStrategy adapter = factory.crearDiscordNotificador();
        Notificacion notificacion = new Notificacion("FINALIZADO", adapter.getCanal(), "Scrim finalizado");

        adapter.enviar(crearUsuario(), notificacion);

        assertEquals("DISCORD", adapter.getCanal());
        assertEquals("ENVIADA", notificacion.getEstado().getNombre());
    }

    private Usuario crearUsuario() {
        Usuario usuario = new Usuario("Alpha", "alpha@mail.com", "hash", "SA");
        usuario.verificarEmail();
        return usuario;
    }
}
