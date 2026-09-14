package com.artverse.marketplace;

import com.artverse.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/marketplace")
public class MarketplaceController {
    private final MarketplaceService service;

    public MarketplaceController(MarketplaceService service) { this.service = service; }

    @GetMapping("/listings")
    public Page<MarketplaceDtos.ListingResponse> listings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        return service.listActive(page, size);
    }

    @GetMapping("/listings/{id}")
    public MarketplaceDtos.ListingResponse listing(@PathVariable UUID id) {
        return service.getListing(id);
    }

    @PostMapping("/listings/{artworkId}")
    public MarketplaceDtos.ListingResponse createListing(
            @PathVariable UUID artworkId,
            @Valid @RequestBody MarketplaceDtos.CreateListingRequest request,
            @AuthenticationPrincipal User user) {
        return service.createListing(artworkId, request, user);
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<Void> cancelListing(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        service.cancelListing(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/{listingId}")
    public MarketplaceDtos.OrderResponse createOrder(
            @PathVariable UUID listingId,
            @AuthenticationPrincipal User user) {
        return service.createOrder(listingId, user);
    }

    @GetMapping("/orders/me")
    public Page<MarketplaceDtos.OrderResponse> myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user) {
        return service.myOrders(user, page, size);
    }
}
