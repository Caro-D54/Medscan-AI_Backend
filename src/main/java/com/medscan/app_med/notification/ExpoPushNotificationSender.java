package com.medscan.app_med.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExpoPushNotificationSender implements PushNotificationSender {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String url;

    public ExpoPushNotificationSender(ObjectMapper objectMapper,
                                      RestClient.Builder restClientBuilder,
                                      @Value("${notification.expo.url:https://exp.host/--/api/v2/push/send}") String url) {
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.build();
        this.url = url;
    }

    @Override
    public void send(PushNotification notification) {
        sendPayload(buildRequest(notification));
    }

    private void sendPayload(String payload) {
        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    private String buildRequest(PushNotification notification) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("to", notification.token());
        message.put("title", notification.title());
        message.put("body", notification.body());
        if (notification.categoryId() != null && !notification.categoryId().isBlank()) {
            message.put("categoryId", notification.categoryId());
        }
        if (notification.channelId() != null && !notification.channelId().isBlank()) {
            message.put("channelId", notification.channelId());
        }
        if (notification.data() != null && !notification.data().isEmpty()) {
            message.put("data", notification.data());
        }
        return writeJson(List.of(message));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new PushSendingException("No se pudo construir la petición de notificación", e);
        }
    }
}
