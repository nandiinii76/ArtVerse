package com.artverse.auction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auctions")
@Getter @Setter @NoArgsConstructor
public class Auction {
    @Id @GeneratedValue private UUID id;
    @Column(name = "artwork_id", nullable = false, unique = true) private UUID artworkId;
    @Column(name = "seller_id", nullable = false) private UUID sellerId;
    @Column(name = "starting_price", nullable = false, precision = 14, scale = 2) private BigDecimal startingPrice;
    @Column(name = "current_price", nullable = false, precision = 14, scale = 2) private BigDecimal currentPrice;
    @Column(name = "minimum_increment", nullable = false, precision = 14, scale = 2) private BigDecimal minimumIncrement;
    @Column(nullable = false, length = 10) private String currency = "INR";
    @Column(name = "starts_at", nullable = false) private Instant startsAt;
    @Column(name = "ends_at", nullable = false) private Instant endsAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AuctionStatus status = AuctionStatus.SCHEDULED;
    @Column(name = "winner_id") private UUID winnerId;
    @Column(name = "winner_notified", nullable = false) private boolean winnerNotified;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
