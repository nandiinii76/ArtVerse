package com.artverse.media;

import java.time.Instant;
import java.util.UUID;

public final class MediaDtos {
    private MediaDtos() {}

    public record Response(UUID id, UUID artworkId, String originalName, String url,
                           String contentType, long fileSize, String checksum, Instant createdAt) {
        static Response from(MediaAsset asset) {
            return new Response(asset.getId(), asset.getArtworkId(), asset.getOriginalName(),
                    "/api/v1/media/" + asset.getId(), asset.getContentType(), asset.getFileSize(),
                    asset.getChecksum(), asset.getCreatedAt());
        }
    }
}
