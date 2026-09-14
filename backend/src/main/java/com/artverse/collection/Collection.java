package com.artverse.collection;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "collections", indexes = {
        @Index(name = "idx_collections_owner", columnList = "owner_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Collection {
    @Id @GeneratedValue
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "cover_artwork_id")
    private UUID coverArtworkId;

    @Column(name = "is_public", nullable = false)
    private boolean publicCollection = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
}
