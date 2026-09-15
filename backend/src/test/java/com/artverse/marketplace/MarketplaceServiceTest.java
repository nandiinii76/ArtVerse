package com.artverse.marketplace;

import com.artverse.artwork.Artwork;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.artwork.ArtworkStatus;
import com.artverse.notification.NotificationEventService;
import com.artverse.user.User;
import com.artverse.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketplaceServiceTest {
    @Mock MarketplaceListingRepository listings;
    @Mock OrderRepository orders;
    @Mock ArtworkOwnershipRepository ownership;
    @Mock ArtworkRepository artworks;
    @Mock UserRepository users;
    @Mock NotificationEventService notificationEvents;
    @InjectMocks MarketplaceService service;

    @Test
    void sellerCannotPurchaseOwnListing() {
        UUID sellerId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID artworkId = UUID.randomUUID();
        User seller = new User();
        seller.setId(sellerId);
        MarketplaceListing listing = new MarketplaceListing();
        listing.setId(listingId);
        listing.setArtworkId(artworkId);
        listing.setSellerId(sellerId);
        listing.setPrice(new BigDecimal("1000.00"));
        listing.setCurrency("INR");
        listing.setStatus(ListingStatus.ACTIVE);
        when(listings.findWithLockById(listingId)).thenReturn(java.util.Optional.of(listing));

        assertThrows(com.artverse.common.ApiException.class,
                () -> service.createOrder(listingId, seller));
        verify(orders, never()).save(any());
    }

    @Test
    void cannotListUnpublishedArtwork() {
        UUID artworkId = UUID.randomUUID();
        User seller = new User();
        UUID sellerId = UUID.randomUUID();
        seller.setId(sellerId);
        Artwork artwork = new Artwork();
        artwork.setId(artworkId);
        artwork.setArtistId(sellerId);
        artwork.setStatus(ArtworkStatus.DRAFT);
        when(artworks.findById(artworkId)).thenReturn(java.util.Optional.of(artwork));

        MarketplaceDtos.CreateListingRequest request = new MarketplaceDtos.CreateListingRequest(
                new BigDecimal("1000.00"), "INR");

        assertThrows(com.artverse.common.ApiException.class,
                () -> service.createListing(artworkId, request, seller));
        verify(listings, never()).save(any());
    }
}
