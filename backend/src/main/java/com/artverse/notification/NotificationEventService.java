package com.artverse.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationEventService {
    private final NotificationService notifications;

    public void favorite(UUID artistId, UUID actorId, String artworkTitle) {
        if (!same(artistId, actorId)) notifications.create(artistId, "ARTWORK_FAVORITED", "Artwork saved", "Someone saved your artwork \"" + artworkTitle + "\".");
    }

    public void comment(UUID artistId, UUID actorId, String artworkTitle) {
        if (!same(artistId, actorId)) notifications.create(artistId, "ARTWORK_COMMENTED", "New comment", "Someone commented on your artwork \"" + artworkTitle + "\".");
    }

    public void follow(UUID artistId, UUID followerId) {
        if (!same(artistId, followerId)) notifications.create(artistId, "NEW_FOLLOWER", "New follower", "Someone started following your work.");
    }

    public void auctionBid(UUID sellerId, UUID bidderId, String artworkTitle) {
        if (!same(sellerId, bidderId)) notifications.create(sellerId, "AUCTION_BID", "New auction bid", "A new bid was placed on \"" + artworkTitle + "\".");
    }

    public void auctionWon(UUID winnerId, String artworkTitle) {
        notifications.create(winnerId, "AUCTION_WON", "Auction won", "You won the auction for \"" + artworkTitle + "\".");
    }

    public void marketplaceOrder(UUID sellerId, UUID buyerId, String artworkTitle) {
        if (!same(sellerId, buyerId)) notifications.create(sellerId, "MARKETPLACE_ORDER", "Artwork purchase started", "Someone started an order for \"" + artworkTitle + "\".");
    }

    public void marketplaceOrderPaid(UUID sellerId, UUID buyerId, String artworkTitle) {
        if (!same(sellerId, buyerId)) {
            notifications.create(sellerId, "MARKETPLACE_ORDER_PAID", "Artwork sold", "Your artwork \"" + artworkTitle + "\" has been purchased.");
            notifications.create(buyerId, "MARKETPLACE_PURCHASED", "Purchase confirmed", "Your purchase of \"" + artworkTitle + "\" is complete.");
        }
    }

    private boolean same(UUID a, UUID b) { return a != null && a.equals(b); }
}
