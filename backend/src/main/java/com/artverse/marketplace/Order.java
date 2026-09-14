package com.artverse.marketplace;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor
public class Order {
    @Id @GeneratedValue private UUID id;
    @Column(name = "buyer_id", nullable = false) private UUID buyerId;
    @Column(name = "artwork_id", nullable = false) private UUID artworkId;
    @Column(name = "seller_id") private UUID sellerId;
    @Column(name = "auction_id", unique = true) private UUID auctionId;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 10) private String currency = "INR";
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private OrderStatus status = OrderStatus.PENDING;
    @Column(name = "payment_reference", length = 255) private String paymentReference;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
}
