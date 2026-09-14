package com.artverse.artist;

import java.time.Instant;
import java.util.UUID;

public final class ArtistPublicDtos {
    private ArtistPublicDtos() {}

    public record ProfileResponse(
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

    public record ArtworkResponse(
            UUID id,
            String title,
            String description,
            String imageUrl,
            String category,
            String medium,
            String style,
            Integer yearCreated,
            String status,
            java.math.BigDecimal price,
            String currency) {}
}
