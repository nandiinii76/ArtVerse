package com.artverse.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationRealtimeService {
    private final SimpMessagingTemplate messaging;

    public void publish(Notification notification) {
        if (notification.getUserId() == null) return;
        messaging.convertAndSendToUser(notification.getUserId().toString(), "/queue/notifications", notification);
    }
}
