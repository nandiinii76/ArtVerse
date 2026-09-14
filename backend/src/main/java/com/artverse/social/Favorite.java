package com.artverse.social;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "artwork_favorites")
public class Favorite {
    @EmbeddedId
    private FavoriteId id;
    protected Favorite() {}
    public Favorite(FavoriteId id) { this.id = id; }
    public FavoriteId getId() { return id; }
}

@Embeddable
class FavoriteId implements Serializable {
    private UUID artworkId;
    private UUID userId;
    protected FavoriteId() {}
    public FavoriteId(UUID artworkId, UUID userId) { this.artworkId = artworkId; this.userId = userId; }
    public UUID getArtworkId() { return artworkId; }
    public UUID getUserId() { return userId; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof FavoriteId other)) return false; return java.util.Objects.equals(artworkId, other.artworkId) && java.util.Objects.equals(userId, other.userId); }
    @Override public int hashCode() { return java.util.Objects.hash(artworkId, userId); }
}
