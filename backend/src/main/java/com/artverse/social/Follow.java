package com.artverse.social;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_follows")
public class Follow {
    @EmbeddedId
    private FollowId id;

    protected Follow() {}

    public Follow(FollowId id) { this.id = id; }

    public FollowId getId() { return id; }
}

@Embeddable
class FollowId implements Serializable {
    private UUID followerId;
    private UUID followingId;

    protected FollowId() {}

    public FollowId(UUID followerId, UUID followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }

    public UUID getFollowerId() { return followerId; }
    public UUID getFollowingId() { return followingId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FollowId other)) return false;
        return Objects.equals(followerId, other.followerId)
                && Objects.equals(followingId, other.followingId);
    }

    @Override
    public int hashCode() { return Objects.hash(followerId, followingId); }
}
