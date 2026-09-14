package com.artverse.notification;

import com.artverse.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationRepository notifications;

    public NotificationController(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public Page<NotificationDtos.Response> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user) {
        return notifications.findByUserIdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50)))
                .map(NotificationDtos.Response::from);
    }

    @GetMapping("/unread-count")
    public NotificationDtos.UnreadCount unreadCount(@AuthenticationPrincipal User user) {
        return new NotificationDtos.UnreadCount(notifications.countByUserIdAndReadAtIsNull(user.getId()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        Notification notification = notifications.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.getUserId().equals(user.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You cannot modify this notification");
        }
        notification.setReadAt(notification.getReadAt() == null ? Instant.now() : notification.getReadAt());
        notifications.save(notification);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal User user) {
        notifications.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 1000))
                .forEach(notification -> {
                    if (notification.getReadAt() == null) notification.setReadAt(Instant.now());
                });
        notifications.flush();
        return ResponseEntity.noContent().build();
    }
}
