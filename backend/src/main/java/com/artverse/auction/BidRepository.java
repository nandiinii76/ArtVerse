package com.artverse.auction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BidRepository extends JpaRepository<Bid, UUID> {
    Page<Bid> findByAuctionIdOrderByAmountDescCreatedAtDesc(UUID auctionId, Pageable pageable);
}
