package com.queerxdisasster.websocketnotifications.service;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WebSocketSessionManager {

    // userId -> список WebSocket-сессий
    private final Map<String, List<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addSession(String userId, WebSocketSession session) {
        System.out.println("added session");
        sessionsByUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(session);
    }

    public void removeSession(String userId, WebSocketSession session) {
        var list = sessionsByUser.getOrDefault(userId, Collections.emptyList());
        list.remove(session);
        if (list.isEmpty()) {
            sessionsByUser.remove(userId);
        }
    }

    public List<WebSocketSession> getSessionsForUser(String userId) {
        return sessionsByUser.getOrDefault(userId, Collections.emptyList());
    }

    public void sendMessage(WebSocketSession session, String type, Object payload) {
        try {
            var map = new HashMap<String, Object>();
            map.put("type", type);
            map.put("data", payload);
            String json = objectMapper.writeValueAsString(map);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
