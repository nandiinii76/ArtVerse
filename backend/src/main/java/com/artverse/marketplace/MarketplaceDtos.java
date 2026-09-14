package com.artverse.marketplace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class MarketplaceDtos {
    private MarketplaceDtos() {}

    public record CreateListingRequest(
            @NotNull @DecimalMin("0.01") BigDecimal price,
            String currency) {}

    public record ListingResponse(
            UUID id, UUID artworkId, UUID sellerId, BigDecimal price,
            String currency, ListingStatus status, Instant createdAt) {
        static ListingResponse from(MarketplaceListing l) {
            return new ListingResponse(l.getId(), l.getArtworkId(), l.getSellerId(), l.getPrice(),
                    l.getCurrency(), l.getStatus(), l.getCreatedAt());
        }
    }

    public record OrderResponse(
            UUID id, UUID buyerId, UUID artworkId, BigDecimal amount,
            String currency, OrderStatus status, String paymentReference,
            Instant createdAt, Instant updatedAt) {
        static OrderResponse from(Order o) {
            return new OrderResponse(o.getId(), o.getBuyerId(), o.getArtworkId(), o.getAmount(),
                    o.getCurrency(), o.getStatus(), o.getPaymentReference(), o.getCreatedAt(), o.getUpdatedAt());
        }
    }
}
