package com.artverse.artwork;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ArtworkDtos {
    private ArtworkDtos() {}

    public record CreateRequest(
            @NotBlank @Size(max = 180) String title,
            @Size(max = 5000) String description,
            @Size(max = 1000) String imageUrl,
            @NotBlank @Size(max = 80) String category,
            @Size(max = 100) String style,
            @Size(max = 120) String medium,
            @Min(1) @Max(3000) Integer yearCreated,
            @DecimalMin("0.00") BigDecimal price,
            @Size(max = 10) String currency,
            ArtworkStatus status,
            Boolean featured) {}

    public record Response(UUID id, String title, String description, String imageUrl, UUID artistId,
                           String category, String style, String medium, Integer yearCreated,
                           BigDecimal price, String currency, ArtworkStatus status, boolean featured,
                           long views, Instant createdAt) {
        public static Response from(Artwork a) {
            return new Response(a.getId(), a.getTitle(), a.getDescription(), a.getImageUrl(), a.getArtistId(),
                    a.getCategory(), a.getStyle(), a.getMedium(), a.getYearCreated(), a.getPrice(),
                    a.getCurrency(), a.getStatus(), a.isFeatured(), a.getViews(), a.getCreatedAt());
        }
    }
}
