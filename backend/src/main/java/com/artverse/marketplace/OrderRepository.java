package com.artverse.marketplace;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId, Pageable pageable);
    Page<Order> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);
    Optional<Order> findByAuctionId(UUID auctionId);
    java.util.List<Order> findByArtworkIdOrderByCreatedAtAsc(UUID artworkId);
}
