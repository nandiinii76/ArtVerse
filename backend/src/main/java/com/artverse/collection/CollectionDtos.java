package com.artverse.collection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class CollectionDtos {
    private CollectionDtos() {}

    public record CreateRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 1000) String description,
            Boolean publicCollection) {}

    public record Response(
            UUID id, UUID ownerId, String name, String description,
            UUID coverArtworkId, boolean publicCollection,
            long artworkCount, Instant createdAt) {}

    public record UpdateRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 1000) String description,
            Boolean publicCollection,
            UUID coverArtworkId) {}

    public record ArtworkResponse(UUID collectionId, UUID artworkId, Instant addedAt) {}
}
