package com.artverse.collection;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "collection_artworks")
@Getter
@NoArgsConstructor
public class CollectionArtwork {
    @EmbeddedId
    private CollectionArtworkId id;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    public CollectionArtwork(CollectionArtworkId id) {
        this.id = id;
    }

    @PrePersist
    void onCreate() { addedAt = Instant.now(); }
}
