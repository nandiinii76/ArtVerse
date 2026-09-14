package com.artverse.provenance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ProvenanceDtos {
    private ProvenanceDtos() {}

    public record Transaction(UUID orderId, UUID sellerId, UUID buyerId, UUID auctionId,
                              BigDecimal amount, String currency, String status, Instant createdAt,
                              String paymentReference) {}

    public record Response(UUID artworkId, String title, UUID artistId, UUID currentOwnerId,
                           String certificateNumber, Instant issuedAt, List<Transaction> transactions) {}
}
