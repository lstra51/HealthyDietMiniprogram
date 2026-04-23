package com.cupk.healthy_diet.config;

import com.cupk.healthy_diet.security.AuthTokenManager;
import com.cupk.healthy_diet.service.SparkChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final SparkChatService sparkChatService;
    private final AuthTokenManager authTokenManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        ChatRequest request = objectMapper.readValue(payload, ChatRequest.class);
        AuthTokenManager.AuthUser authUser = getAuthUser(session);

        sparkChatService.streamChat(
            authUser.id(),
            request.getMessage(),
            (List<Map<String, String>>) (List<?>) request.getHistory(),
            new SparkChatService.StreamCallback() {
                @Override
                public void onMessage(String content) {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                                Map.of("type", "message", "content", content)
                            )));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onComplete() {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                                Map.of("type", "complete")
                            )));
                            session.close(CloseStatus.NORMAL);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String error) {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                                Map.of("type", "error", "content", error)
                            )));
                            session.close(CloseStatus.SERVER_ERROR);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        );
    }

    private AuthTokenManager.AuthUser getAuthUser(WebSocketSession session) {
        MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams();
        return authTokenManager.parseToken(params.getFirst("token"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        exception.printStackTrace();
    }

    public static class ChatRequest {
        private Integer userId;
        private String message;
        private List<Map<String, Object>> history;

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<Map<String, Object>> getHistory() {
            return history;
        }

        public void setHistory(List<Map<String, Object>> history) {
            this.history = history;
        }
    }
}
