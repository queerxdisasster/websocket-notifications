package com.queerxdisasster.websocketnotifications.service;

import com.queerxdisasster.handlers.NotificationEventHandler;
import com.queerxdisasster.handlers.HistoryResponseEventHandler;
import com.queerxdisasster.listeners.HistoryResponseEventListener;
import com.queerxdisasster.listeners.NotificationEventListener;
import com.queerxdisasster.shared.NotificationEvent;
import com.queerxdisasster.shared.HistoryResponseEvent;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteMessaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * B2ConsumerService:
 *  - Реализует NotificationEventHandler, HistoryResponseEventHandler
 *  - Можно подписываться на 2 топика (notifications, historyResponseTopic)
 */
@Service
public class B2ConsumerService implements NotificationEventHandler, HistoryResponseEventHandler {

    private static final String NOTIFICATION_TOPIC = "notifications";
    private static final String HISTORY_RESPONSE_TOPIC = "historyResponseTopic";

    @Autowired
    private Ignite ignite;

    @Autowired
    private transient WebSocketSessionManager sessionManager;

    @PostConstruct
    public void init() {
        IgniteMessaging messaging = ignite.message();

        // Листенер для NotificationEvent:
        messaging.remoteListen(NOTIFICATION_TOPIC, new NotificationEventListener(this));

        // Листенер для HistoryResponseEvent:
        messaging.remoteListen(HISTORY_RESPONSE_TOPIC, new HistoryResponseEventListener(this));
    }

    // Реализация NotificationEventHandler:
    @Override
    public void handleNotification(NotificationEvent event) {
        System.out.println("B2ConsumerService handling notification: " + event);
        System.out.println("i got notification");
        var sessions = sessionManager.getSessionsForUser(event.getUserId());
        if (!sessions.isEmpty()) {
            for (var session : sessions) {
                sessionManager.sendMessage(session, "NOTIFICATION", event);
            }
        }
    }

    // Реализация HistoryResponseEventHandler:
    @Override
    public void handleHistoryResponse(HistoryResponseEvent event) {
        System.out.println("B2ConsumerService handling history response: " + event);
        var sessions = sessionManager.getSessionsForUser(event.getUserId());
        if (!sessions.isEmpty()) {
            for (var session : sessions) {
                sessionManager.sendMessage(session, "HISTORY_RESPONSE", event);
            }
        }
    }
}
