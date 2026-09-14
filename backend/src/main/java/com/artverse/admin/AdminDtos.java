package com.artverse.admin;

import com.artverse.artwork.Artwork;
import com.artverse.user.User;

import java.time.Instant;
import java.util.UUID;

public final class AdminDtos {
    private AdminDtos() {}

    public record PlatformStats(long users, long artists, long artworks, long marketplaceListings, long auctions) {}

    public record UserSummary(UUID id, String displayName, String email, boolean enabled, boolean emailVerified, String roles, Instant createdAt) {
        static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getDisplayName(), user.getEmail(), user.isEnabled(), user.isEmailVerified(),
                    user.getRoles().stream().map(Enum::name).sorted().reduce((a, b) -> a + ", " + b).orElse(""), user.getCreatedAt());
        }
    }

    public record ArtworkSummary(UUID id, String title, UUID artistId, String imageUrl, String category, String status, Instant createdAt) {
        static ArtworkSummary from(Artwork artwork) {
            return new ArtworkSummary(artwork.getId(), artwork.getTitle(), artwork.getArtistId(), artwork.getImageUrl(),
                    artwork.getCategory(), artwork.getStatus().name(), artwork.getCreatedAt());
        }
    }
}
