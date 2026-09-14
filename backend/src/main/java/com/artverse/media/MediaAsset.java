package com.artverse.media;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_assets", indexes = {
        @Index(name = "idx_media_artwork", columnList = "artwork_id"),
        @Index(name = "idx_media_owner", columnList = "owner_id")
})
@Getter @Setter @NoArgsConstructor
public class MediaAsset {
    @Id @GeneratedValue
    private UUID id;
    @Column(name = "artwork_id", nullable = false) private UUID artworkId;
    @Column(name = "owner_id", nullable = false) private UUID ownerId;
    @Column(name = "original_name", nullable = false, length = 255) private String originalName;
    @Column(name = "storage_key", nullable = false, unique = true, length = 500) private String storageKey;
    @Column(name = "provider", nullable = false, length = 30) private String provider = "local";
    @Column(name = "resource_url", nullable = false, length = 1500) private String resourceUrl;
    @Column(name = "content_type", nullable = false, length = 100) private String contentType;
    @Column(name = "file_size", nullable = false) private long fileSize;
    @Column(name = "checksum", nullable = false, length = 64) private String checksum;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @PrePersist void create() { createdAt = Instant.now(); }
}
