package com.artverse.collection;

import jakarta.persistence.Embeddable;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@NoArgsConstructor
public class CollectionArtworkId implements Serializable {
    private UUID collectionId;
    private UUID artworkId;

    public CollectionArtworkId(UUID collectionId, UUID artworkId) {
        this.collectionId = collectionId;
        this.artworkId = artworkId;
    }

    public UUID getCollectionId() { return collectionId; }
    public UUID getArtworkId() { return artworkId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectionArtworkId other)) return false;
        return Objects.equals(collectionId, other.collectionId) && Objects.equals(artworkId, other.artworkId);
    }

    @Override
    public int hashCode() { return Objects.hash(collectionId, artworkId); }
}
