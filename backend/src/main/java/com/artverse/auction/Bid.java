package com.artverse.auction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auction_bids")
@Getter @Setter @NoArgsConstructor
public class Bid {
    @Id @GeneratedValue private UUID id;
    @Column(name = "auction_id", nullable = false) private UUID auctionId;
    @Column(name = "bidder_id", nullable = false) private UUID bidderId;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal amount;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
