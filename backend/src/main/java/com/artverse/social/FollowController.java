package com.artverse.social;

import com.artverse.notification.NotificationEventService;
import com.artverse.user.User;
import com.artverse.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/social/follows")
public class FollowController {
    private final FollowRepository follows;
    private final NotificationEventService notifications;
    private final UserRepository users;

    public FollowController(FollowRepository follows, NotificationEventService notifications, UserRepository users) {
        this.follows = follows;
        this.notifications = notifications;
        this.users = users;
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

    @GetMapping("/{artistId}/followers")
    public Page<FollowDtos.FollowerResponse> followers(
            @PathVariable UUID artistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Page<Follow> followPage = follows.findByIdFollowingId(
                artistId, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
        List<UUID> ids = followPage.getContent().stream()
                .map(f -> f.getId().getFollowerId())
                .toList();
        Map<UUID, User> byId = users.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<FollowDtos.FollowerResponse> content = ids.stream()
                .map(byId::get)
                .filter(u -> u != null)
                .map(u -> new FollowDtos.FollowerResponse(u.getId(), u.getDisplayName(), u.getAvatarUrl()))
                .toList();
        return new PageImpl<>(content, followPage.getPageable(), followPage.getTotalElements());
    }
}
