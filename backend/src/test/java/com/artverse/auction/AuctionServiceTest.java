package com.artverse.auction;

import com.artverse.artwork.ArtworkRepository;
import com.artverse.marketplace.MarketplaceService;
import com.artverse.notification.NotificationEventService;
import com.artverse.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {
    @Mock AuctionRepository auctions;
    @Mock BidRepository bids;
    @Mock ArtworkRepository artworks;
    @Mock NotificationEventService notificationEvents;
    @Mock MarketplaceService marketplace;
    @Mock SimpMessagingTemplate messaging;
    @InjectMocks AuctionService service;

    @Test
    void sellerCannotBidOnOwnAuction() {
        UUID auctionId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        User seller = new User();
        seller.setId(sellerId);
        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setSellerId(sellerId);
        auction.setStatus(AuctionStatus.LIVE);
        auction.setCurrentPrice(new BigDecimal("1000.00"));
        auction.setMinimumIncrement(new BigDecimal("100.00"));
        when(auctions.findWithLockById(auctionId)).thenReturn(java.util.Optional.of(auction));

        AuctionDtos.BidRequest request = new AuctionDtos.BidRequest(new BigDecimal("1200.00"));

        assertThrows(com.artverse.common.ApiException.class,
                () -> service.bid(auctionId, request, seller));
        verify(bids, never()).save(any());
        verify(messaging, never()).convertAndSend(anyString(), any());
    }

    @Test
    void rejectsBidBelowMinimumIncrement() {
        UUID auctionId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        User bidder = new User();
        bidder.setId(UUID.randomUUID());
        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setSellerId(sellerId);
        auction.setStatus(AuctionStatus.LIVE);
        auction.setCurrentPrice(new BigDecimal("1000.00"));
        auction.setMinimumIncrement(new BigDecimal("100.00"));
        when(auctions.findWithLockById(auctionId)).thenReturn(java.util.Optional.of(auction));

        AuctionDtos.BidRequest request = new AuctionDtos.BidRequest(new BigDecimal("1050.00"));

        assertThrows(com.artverse.common.ApiException.class,
                () -> service.bid(auctionId, request, bidder));
        verify(bids, never()).save(any());
    }
}
