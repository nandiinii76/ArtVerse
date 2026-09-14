package com.artverse.artist;

import java.time.Instant;
import java.util.UUID;

public final class ArtistDirectoryDtos {
    private ArtistDirectoryDtos() {}

    public record Response(
            UUID userId,
            String displayName,
            String biography,
            String location,
            String websiteUrl,
            String profileImageUrl,
            boolean verified,
            Instant joinedAt,
            long artworkCount,
            long followerCount) {}
}
