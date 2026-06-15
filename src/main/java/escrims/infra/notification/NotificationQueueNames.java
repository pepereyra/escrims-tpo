package escrims.infra.notification;

public final class NotificationQueueNames {

    public static final String EXCHANGE = "escrims.notifications";
    public static final String QUEUE = "escrims.notifications.dispatch";
    public static final String ROUTING_KEY = "notification.dispatch";

    private NotificationQueueNames() {
    }
}
