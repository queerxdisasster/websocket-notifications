package com.queerxdisasster.websocketnotifications.service;


import com.queerxdisasster.shared.HistoryRequestEvent;
import com.queerxdisasster.shared.ReadStatusEvent;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteMessaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class B2ProducerService {

    private static final String READ_STATUS_TOPIC = "readStatusTopic";
    private static final String HISTORY_REQUEST_TOPIC = "historyRequestTopic";

    private final IgniteMessaging messaging;

    @Autowired
    public B2ProducerService(Ignite ignite) {
        this.messaging = ignite.message();
    }

    public void sendReadStatus(Long notifId, String userId) {
        ReadStatusEvent event = new ReadStatusEvent(notifId, userId);
        messaging.send(READ_STATUS_TOPIC, event);
    }

    public void sendHistoryRequest(String userId, int limit) {
        HistoryRequestEvent event = new HistoryRequestEvent(userId, limit);
        messaging.send(HISTORY_REQUEST_TOPIC, event);
    }
}
