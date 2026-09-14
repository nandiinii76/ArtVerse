package com.artverse.social;

import com.artverse.notification.NotificationEventService;
import com.artverse.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social/follows")
public class FollowController {
    private final FollowRepository follows;
    private final NotificationEventService notifications;

    public FollowController(FollowRepository follows, NotificationEventService notifications) {
        this.follows = follows;
        this.notifications = notifications;
    }

    @PostMapping("/{artistId}")
    public FollowDtos.FollowResponse follow(@PathVariable UUID artistId, @AuthenticationPrincipal User user) {
        if (artistId.equals(user.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "You cannot follow yourself");
        }
        boolean alreadyFollowing = follows.existsByIdFollowerIdAndIdFollowingId(user.getId(), artistId);
        if (!alreadyFollowing) {
            follows.save(new Follow(new FollowId(user.getId(), artistId)));
            notifications.follow(artistId, user.getId());
        }
        return new FollowDtos.FollowResponse(user.getId(), artistId, true);
    }

    @DeleteMapping("/{artistId}")
    public ResponseEntity<Void> unfollow(@PathVariable UUID artistId, @AuthenticationPrincipal User user) {
        follows.deleteById(new FollowId(user.getId(), artistId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{artistId}")
    public FollowDtos.FollowResponse status(@PathVariable UUID artistId, @AuthenticationPrincipal User user) {
        return new FollowDtos.FollowResponse(user.getId(), artistId,
                follows.existsByIdFollowerIdAndIdFollowingId(user.getId(), artistId));
    }

    @GetMapping("/{artistId}/stats")
    public FollowDtos.FollowStats stats(@PathVariable UUID artistId) {
        return new FollowDtos.FollowStats(
                follows.countByIdFollowingId(artistId),
                follows.countByIdFollowerId(artistId));
    }
}
