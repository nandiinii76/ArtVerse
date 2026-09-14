package com.artverse.notification;

import java.util.UUID;

public final class NotificationFactory {
    private NotificationFactory() {}

    public static Notification build(UUID userId, String type, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        return notification;
    }
}
