package com.artverse.auction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class AuctionDtos {
    private AuctionDtos() {}
    public record CreateRequest(@NotNull @DecimalMin("0.01") BigDecimal startingPrice,
                                @NotNull @DecimalMin("0.01") BigDecimal minimumIncrement,
                                String currency, @NotNull Instant startsAt, @NotNull Instant endsAt) {}
    public record BidRequest(@NotNull @DecimalMin("0.01") BigDecimal amount) {}
    public record Response(UUID id, UUID artworkId, UUID sellerId, BigDecimal startingPrice,
                           BigDecimal currentPrice, BigDecimal minimumIncrement, String currency,
                           Instant startsAt, Instant endsAt, AuctionStatus status, UUID winnerId) {
        static Response from(Auction a) { return new Response(a.getId(), a.getArtworkId(), a.getSellerId(),
                a.getStartingPrice(), a.getCurrentPrice(), a.getMinimumIncrement(), a.getCurrency(),
                a.getStartsAt(), a.getEndsAt(), a.getStatus(), a.getWinnerId()); }
    }
    public record BidResponse(UUID id, UUID auctionId, UUID bidderId, BigDecimal amount, Instant createdAt) {
        static BidResponse from(Bid b) { return new BidResponse(b.getId(), b.getAuctionId(), b.getBidderId(), b.getAmount(), b.getCreatedAt()); }
    }
}
