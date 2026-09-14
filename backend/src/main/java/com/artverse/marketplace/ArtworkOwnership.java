package com.artverse.marketplace;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "artwork_ownership")
@Getter
@Setter
@NoArgsConstructor
public class ArtworkOwnership {
    @Id @GeneratedValue
    private UUID id;
    @Column(name = "artwork_id", nullable = false) private UUID artworkId;
    @Column(name = "owner_id", nullable = false) private UUID ownerId;
    @Column(nullable = false, updatable = false) private Instant acquiredAt;
    @Column(name = "transaction_id") private UUID transactionId;
    @PrePersist void onCreate() { acquiredAt = Instant.now(); }
}
