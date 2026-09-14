package com.artverse.notification;

import java.time.Instant;
import java.util.UUID;

public final class NotificationDtos {
    private NotificationDtos() {}

    public record Response(UUID id, String type, String title, String message, Instant readAt, Instant createdAt) {
        static Response from(Notification n) {
            return new Response(n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.getReadAt(), n.getCreatedAt());
        }
    }

    public record UnreadCount(long count) {}
}
