package com.artverse.social;

import java.util.UUID;

public final class FollowDtos {
    private FollowDtos() {}

    public record FollowResponse(UUID followerId, UUID followingId, boolean following) {}
    public record FollowStats(long followers, long following) {}
    public record FollowerResponse(UUID userId, String displayName, String avatarUrl) {}
}
