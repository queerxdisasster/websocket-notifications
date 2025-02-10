package com.queerxdisasster.websocketnotifications.websocket;


import com.queerxdisasster.websocketnotifications.service.B2ProducerService;
import com.queerxdisasster.websocketnotifications.service.WebSocketSessionManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MyWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final B2ProducerService producerService;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Из query params или headers можно извлечь userId
        // Упрощённо предположим, что userId передаётся как ?userId=...
        String userId = getUserIdFromSession(session);
        sessionManager.addSession(userId, session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // Предположим, клиент шлёт JSON {"action": "...", "notificationId": 123, ...}
            System.out.println("notif landed1");
            Map<String, Object> map = objectMapper.readValue(message.getPayload(), Map.class);
            String action = (String) map.get("action");
            String userId = getUserIdFromSession(session);

            if ("READ_NOTIFICATION".equalsIgnoreCase(action)) {
                // notificationId
                Number notifIdNum = (Number) map.get("notificationId");
                Long notifId = notifIdNum.longValue();

                // Шлём событие в Ignite (READ_STATUS_TOPIC)
                if (producerService != null) {
                    producerService.sendReadStatus(notifId, userId);
                }
            } else if ("GET_HISTORY".equalsIgnoreCase(action)) {
                // Попросим N = 20
                System.out.println("get history");
                int limit = map.get("limit") == null ? 20 : ((Number)map.get("limit")).intValue();
                if (producerService != null) {
                    producerService.sendHistoryRequest(userId, limit);
                }
            }
            // ...другие действия
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserIdFromSession(session);
        sessionManager.removeSession(userId, session);
    }

    private String getUserIdFromSession(WebSocketSession session) {
        // Упрощённый метод. Ищем в query параметрах
        String query = session.getUri().getQuery(); // "userId=abc"
        // Разобрать query. Реализуем упрощённо:
        if (query != null && query.startsWith("userId=")) {
            return query.substring("userId=".length());
        }
        return "unknown";
    }
}
