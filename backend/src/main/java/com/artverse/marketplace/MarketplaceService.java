package com.artverse.marketplace;

import com.artverse.artwork.Artwork;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.artwork.ArtworkStatus;
import com.artverse.auction.Auction;
import com.artverse.common.ApiException;
import com.artverse.notification.NotificationEventService;
import com.artverse.user.User;
import com.artverse.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarketplaceService {
    private final MarketplaceListingRepository listings;
    private final OrderRepository orders;
    private final ArtworkOwnershipRepository ownership;
    private final ArtworkRepository artworks;
    private final UserRepository users;
    private final NotificationEventService notificationEvents;

    @Transactional
    public MarketplaceDtos.ListingResponse createListing(UUID artworkId, MarketplaceDtos.CreateListingRequest request, User seller) {
        Artwork artwork = artwork(artworkId);
        if (!artwork.getArtistId().equals(seller.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "NOT_OWNER", "Only the artwork owner can create a listing");
        if (artwork.getStatus() != ArtworkStatus.PUBLISHED) throw ApiException.conflict("ARTWORK_NOT_SELLABLE", "Only published artworks can be listed");
        if (request.price().compareTo(BigDecimal.ZERO) <= 0) throw ApiException.conflict("INVALID_PRICE", "Listing price must be greater than zero");
        if (listings.findByArtworkId(artworkId).filter(l -> l.getStatus() == ListingStatus.ACTIVE).isPresent()) throw ApiException.conflict("ALREADY_LISTED", "This artwork is already listed");
        MarketplaceListing listing = listings.findByArtworkId(artworkId).orElseGet(MarketplaceListing::new);
        listing.setArtworkId(artworkId); listing.setSellerId(seller.getId()); listing.setPrice(request.price()); listing.setCurrency(normalizeCurrency(request.currency())); listing.setStatus(ListingStatus.ACTIVE);
        return listingResponse(listings.save(listing));
    }

    public Page<MarketplaceDtos.ListingResponse> listActive(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        return listings.findByStatus(ListingStatus.ACTIVE, pageable).map(this::listingResponse);
    }

    public Page<MarketplaceDtos.ListingResponse> searchActive(String q, String category, String style, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        List<MarketplaceDtos.ListingResponse> all = listings.findByStatus(ListingStatus.ACTIVE, Pageable.unpaged()).stream().map(this::listingResponse).filter(item -> {
            String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
            String cat = category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
            String st = style == null ? "" : style.trim().toLowerCase(Locale.ROOT);
            boolean text = query.isBlank() || contains(item.title(), query) || contains(item.sellerName(), query) || contains(item.style(), query) || contains(item.medium(), query);
            boolean categoryMatch = cat.isBlank() || contains(item.category(), cat);
            boolean styleMatch = st.isBlank() || contains(item.style(), st);
            boolean min = minPrice == null || item.price().compareTo(minPrice) >= 0;
            boolean max = maxPrice == null || item.price().compareTo(maxPrice) <= 0;
            return text && categoryMatch && styleMatch && min && max;
        }).toList();
        int safePage = Math.max(page, 0), safeSize = Math.min(Math.max(size, 1), 50), from = Math.min(safePage * safeSize, all.size()), to = Math.min(from + safeSize, all.size());
        return new PageImpl<>(all.subList(from, to), PageRequest.of(safePage, safeSize), all.size());
    }

    public Page<MarketplaceDtos.ListingResponse> myListings(User seller, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        return listings.findBySellerIdOrderByCreatedAtDesc(seller.getId(), pageable).map(this::listingResponse);
    }

    public MarketplaceDtos.ListingResponse getListing(UUID id) { return listingResponse(listings.findById(id).orElseThrow(() -> ApiException.notFound("LISTING_NOT_FOUND", "Marketplace listing not found"))); }

    @Transactional
    public void cancelListing(UUID id, User seller) {
        MarketplaceListing listing = listing(id);
        if (!listing.getSellerId().equals(seller.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "NOT_SELLER", "Only the seller can cancel this listing");
        if (listing.getStatus() != ListingStatus.ACTIVE) throw ApiException.conflict("LISTING_NOT_ACTIVE", "This listing is no longer active");
        listing.setStatus(ListingStatus.CANCELLED); listings.save(listing);
    }

    @Transactional
    public MarketplaceDtos.OrderResponse createOrder(UUID listingId, User buyer) {
        MarketplaceListing listing = listings.findWithLockById(listingId).orElseThrow(() -> ApiException.notFound("LISTING_NOT_FOUND", "Marketplace listing not found"));
        if (listing.getStatus() != ListingStatus.ACTIVE) throw ApiException.conflict("LISTING_NOT_ACTIVE", "This artwork is no longer available");
        if (listing.getSellerId().equals(buyer.getId())) throw ApiException.conflict("SELF_PURCHASE", "You cannot purchase your own artwork");
        Order order = new Order(); order.setBuyerId(buyer.getId()); order.setArtworkId(listing.getArtworkId()); order.setSellerId(listing.getSellerId()); order.setAmount(listing.getPrice()); order.setCurrency(listing.getCurrency()); order.setStatus(OrderStatus.PENDING);
        Order saved = orders.save(order); Artwork artwork = artwork(listing.getArtworkId()); notificationEvents.marketplaceOrder(listing.getSellerId(), buyer.getId(), artwork.getTitle()); return MarketplaceDtos.OrderResponse.from(saved);
    }

    @Transactional
    public MarketplaceDtos.OrderResponse createAuctionSettlementOrder(Auction auction) {
        if (auction.getWinnerId() == null) return null;
        if (orders.findByAuctionId(auction.getId()).isPresent()) return orders.findByAuctionId(auction.getId()).map(MarketplaceDtos.OrderResponse::from).orElse(null);
        Artwork artwork = artwork(auction.getArtworkId()); Order order = new Order(); order.setBuyerId(auction.getWinnerId()); order.setSellerId(auction.getSellerId()); order.setAuctionId(auction.getId()); order.setArtworkId(auction.getArtworkId()); order.setAmount(auction.getCurrentPrice()); order.setCurrency(auction.getCurrency()); order.setStatus(OrderStatus.PENDING); Order saved = orders.save(order); notificationEvents.auctionPaymentDue(auction.getWinnerId(), artwork.getTitle(), auction.getCurrentPrice(), auction.getCurrency()); return MarketplaceDtos.OrderResponse.from(saved);
    }

    @Transactional
    public MarketplaceDtos.OrderResponse completeAuctionPayment(UUID orderId, PaymentRequest request, User buyer) {
        Order order = orders.findById(orderId).orElseThrow(() -> ApiException.notFound("ORDER_NOT_FOUND", "Order not found"));
        if (!order.getBuyerId().equals(buyer.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "NOT_BUYER", "Only the winner can pay this order");
        if (order.getAuctionId() == null) throw ApiException.conflict("NOT_AUCTION_ORDER", "This order is not an auction settlement");
        if (order.getStatus() != OrderStatus.PENDING) throw ApiException.conflict("ORDER_NOT_PENDING", "Only pending orders can be paid");
        order.setStatus(OrderStatus.PAID); order.setPaymentReference(request.paymentReference().trim()); ArtworkOwnership transfer = new ArtworkOwnership(); transfer.setArtworkId(order.getArtworkId()); transfer.setOwnerId(buyer.getId()); transfer.setTransactionId(order.getId()); ownership.save(transfer); Artwork artwork = artwork(order.getArtworkId()); notificationEvents.auctionPaymentReceived(order.getSellerId(), buyer.getId(), artwork.getTitle()); return MarketplaceDtos.OrderResponse.from(orders.save(order));
    }

    @Transactional
    public MarketplaceDtos.OrderResponse completePayment(UUID orderId, PaymentRequest request, User buyer) {
        Order order = orders.findById(orderId).orElseThrow(() -> ApiException.notFound("ORDER_NOT_FOUND", "Order not found"));
        if (!order.getBuyerId().equals(buyer.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "NOT_BUYER", "Only the buyer can complete this order");
        if (order.getStatus() != OrderStatus.PENDING) throw ApiException.conflict("ORDER_NOT_PENDING", "Only pending orders can be paid");
        MarketplaceListing listing = listings.findByArtworkId(order.getArtworkId()).orElseThrow(() -> ApiException.notFound("LISTING_NOT_FOUND", "Marketplace listing not found"));
        if (listing.getStatus() != ListingStatus.ACTIVE) throw ApiException.conflict("LISTING_NOT_ACTIVE", "This artwork is no longer available");
        if (!listing.getSellerId().equals(artwork(order.getArtworkId()).getArtistId())) throw ApiException.conflict("LISTING_OWNER_CHANGED", "The listing owner no longer matches the artwork owner");
        order.setStatus(OrderStatus.PAID); order.setPaymentReference(request.paymentReference().trim()); listing.setStatus(ListingStatus.SOLD); ArtworkOwnership transfer = new ArtworkOwnership(); transfer.setArtworkId(order.getArtworkId()); transfer.setOwnerId(buyer.getId()); transfer.setTransactionId(order.getId()); ownership.save(transfer); Order saved = orders.save(order); notificationEvents.marketplaceOrderPaid(listing.getSellerId(), buyer.getId(), artwork(order.getArtworkId()).getTitle()); return MarketplaceDtos.OrderResponse.from(saved);
    }

    public Page<MarketplaceDtos.OrderResponse> myOrders(User buyer, int page, int size) { return orders.findByBuyerIdOrderByCreatedAtDesc(buyer.getId(), PageRequest.of(Math.max(0,page), Math.min(Math.max(size,1),50))).map(MarketplaceDtos.OrderResponse::from); }
    public Page<MarketplaceDtos.OrderResponse> mySales(User seller, int page, int size) { return orders.findBySellerIdOrderByCreatedAtDesc(seller.getId(), PageRequest.of(Math.max(0,page), Math.min(Math.max(size,1),50))).map(MarketplaceDtos.OrderResponse::from); }

    private MarketplaceDtos.ListingResponse listingResponse(MarketplaceListing listing) { Artwork artwork = artwork(listing.getArtworkId()); String sellerName = users.findById(listing.getSellerId()).map(User::getDisplayName).orElse("Unknown artist"); return MarketplaceDtos.ListingResponse.from(listing, sellerName, artwork); }
    private MarketplaceListing listing(UUID id) { return listings.findById(id).orElseThrow(() -> ApiException.notFound("LISTING_NOT_FOUND", "Marketplace listing not found")); }
    private Artwork artwork(UUID id) { return artworks.findById(id).orElseThrow(() -> ApiException.notFound("ARTWORK_NOT_FOUND", "Artwork not found")); }
    private String normalizeCurrency(String currency) { return currency == null || currency.isBlank() ? "INR" : currency.trim().toUpperCase(); }
    private boolean contains(String value, String query) { return value != null && value.toLowerCase(Locale.ROOT).contains(query); }
}
