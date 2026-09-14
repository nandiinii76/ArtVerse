package com.artverse.auction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface AuctionRepository extends JpaRepository<Auction, UUID> {
    Page<Auction> findByStatus(AuctionStatus status, Pageable pageable);
    Optional<Auction> findByArtworkId(UUID artworkId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Auction> findWithLockById(UUID id);
}
