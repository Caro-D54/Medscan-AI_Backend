package com.medscan.app_med.notification;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record PushNotification(
        String token,
        String title,
        String body,
        String categoryId,
        String channelId,
        Map<String, Object> data
) {
    public PushNotification {
        data = (data == null || data.isEmpty())
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(data));
    }

    public PushNotification(String token, String title, String body) {
        this(token, title, body, null, null, Map.of());
    }
}
