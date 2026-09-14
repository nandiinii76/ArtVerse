package com.artverse.auction;

import com.artverse.artwork.Artwork;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.artwork.ArtworkStatus;
import com.artverse.common.ApiException;
import com.artverse.notification.NotificationEventService;
import com.artverse.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctions;
    private final BidRepository bids;
    private final ArtworkRepository artworks;
    private final NotificationEventService notificationEvents;
    private final SimpMessagingTemplate messaging;

    @Transactional
    public AuctionDtos.Response create(UUID artworkId, AuctionDtos.CreateRequest r, User seller) {
        Artwork artwork = artworks.findById(artworkId).orElseThrow(() -> ApiException.notFound("ARTWORK_NOT_FOUND", "Artwork not found"));
        if (!artwork.getArtistId().equals(seller.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "NOT_OWNER", "Only the artwork owner can create an auction");
        if (artwork.getStatus() != ArtworkStatus.PUBLISHED) throw ApiException.conflict("ARTWORK_NOT_AUCTIONABLE", "Only published artworks can enter an auction");
        if (!r.endsAt().isAfter(r.startsAt()) || !r.startsAt().isAfter(Instant.now())) throw ApiException.conflict("INVALID_AUCTION_WINDOW", "Auction must start in the future and end after it starts");
        if (auctions.findByArtworkId(artworkId).filter(a -> a.getStatus() != AuctionStatus.CANCELLED && a.getStatus() != AuctionStatus.ENDED).isPresent()) throw ApiException.conflict("ALREADY_IN_AUCTION", "This artwork already has an active auction");
        Auction a = auctions.findByArtworkId(artworkId).orElseGet(Auction::new);
        a.setArtworkId(artworkId); a.setSellerId(seller.getId()); a.setStartingPrice(r.startingPrice()); a.setCurrentPrice(r.startingPrice()); a.setMinimumIncrement(r.minimumIncrement());
        a.setCurrency(r.currency() == null || r.currency().isBlank() ? "INR" : r.currency().trim().toUpperCase()); a.setStartsAt(r.startsAt()); a.setEndsAt(r.endsAt()); a.setStatus(AuctionStatus.SCHEDULED); a.setWinnerId(null); a.setWinnerNotified(false);
        return AuctionDtos.Response.from(auctions.save(a));
    }

    public Page<AuctionDtos.Response> list(int page, int size) {
        syncDueAuctions();
        Pageable p = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1),50));
        return auctions.findByStatus(AuctionStatus.LIVE, p).map(AuctionDtos.Response::from);
    }

    @Transactional
    public AuctionDtos.Response get(UUID id) { return AuctionDtos.Response.from(sync(auction(id))); }

    @Transactional
    public AuctionDtos.BidResponse bid(UUID id, AuctionDtos.BidRequest r, User bidder) {
        Auction a = sync(auctions.findWithLockById(id).orElseThrow(() -> ApiException.notFound("AUCTION_NOT_FOUND", "Auction not found")));
        if (a.getSellerId().equals(bidder.getId())) throw ApiException.conflict("SELF_BID", "You cannot bid on your own artwork");
        if (a.getStatus() != AuctionStatus.LIVE) throw ApiException.conflict("AUCTION_NOT_LIVE", "Auction is not accepting bids");
        BigDecimal minimum = a.getCurrentPrice().add(a.getMinimumIncrement());
        if (r.amount().compareTo(minimum) < 0) throw ApiException.conflict("BID_TOO_LOW", "Bid must be at least " + minimum);
        Bid b = new Bid(); b.setAuctionId(a.getId()); b.setBidderId(bidder.getId()); b.setAmount(r.amount()); a.setCurrentPrice(r.amount()); auctions.save(a);
        Bid saved = bids.save(b);
        AuctionDtos.BidResponse response = AuctionDtos.BidResponse.from(saved);
        messaging.convertAndSend("/topic/auctions/" + a.getId(), response);
        artworks.findById(a.getArtworkId()).ifPresent(work -> notificationEvents.auctionBid(a.getSellerId(), bidder.getId(), work.getTitle()));
        return response;
    }

    public Page<AuctionDtos.BidResponse> bids(UUID id, int page, int size) {
        auction(id);
        Pageable p = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1),50));
        return bids.findByAuctionIdOrderByAmountDescCreatedAtDesc(id,p).map(AuctionDtos.BidResponse::from);
    }

    @Transactional
    public AuctionDtos.Response close(UUID id, User seller) {
        Auction a = auctions.findWithLockById(id).orElseThrow(() -> ApiException.notFound("AUCTION_NOT_FOUND", "Auction not found"));
        if (!a.getSellerId().equals(seller.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "NOT_SELLER", "Only the seller can close this auction");
        if (a.getStatus() == AuctionStatus.ENDED) return AuctionDtos.Response.from(a);
        if (a.getStatus() != AuctionStatus.LIVE || Instant.now().isBefore(a.getEndsAt())) throw ApiException.conflict("AUCTION_NOT_READY", "Auction can only be closed after its end time");
        finish(a);
        AuctionDtos.Response response = AuctionDtos.Response.from(auctions.save(a));
        messaging.convertAndSend("/topic/auctions/" + a.getId(), response);
        return response;
    }

    @Transactional
    public void syncDueAuctions() {
        Instant now = Instant.now();
        auctions.findByStatus(AuctionStatus.SCHEDULED, PageRequest.of(0, 200)).forEach(a -> {
            if (!now.isBefore(a.getStartsAt())) {
                if (now.isBefore(a.getEndsAt())) a.setStatus(AuctionStatus.LIVE);
                else finish(a);
                auctions.save(a);
                messaging.convertAndSend("/topic/auctions/" + a.getId(), AuctionDtos.Response.from(a));
            }
        });
        auctions.findByStatus(AuctionStatus.LIVE, PageRequest.of(0, 200)).forEach(a -> {
            if (!now.isBefore(a.getEndsAt())) {
                finish(a);
                auctions.save(a);
                messaging.convertAndSend("/topic/auctions/" + a.getId(), AuctionDtos.Response.from(a));
            }
        });
    }

    private Auction sync(Auction a) {
        Instant now=Instant.now();
        if (a.getStatus()==AuctionStatus.SCHEDULED && !now.isBefore(a.getStartsAt()) && now.isBefore(a.getEndsAt())) { a.setStatus(AuctionStatus.LIVE); return auctions.save(a); }
        if ((a.getStatus()==AuctionStatus.SCHEDULED || a.getStatus()==AuctionStatus.LIVE) && !now.isBefore(a.getEndsAt())) { finish(a); return auctions.save(a); }
        return a;
    }

    private void finish(Auction a) {
        a.setStatus(AuctionStatus.ENDED);
        bids.findByAuctionIdOrderByAmountDescCreatedAtDesc(a.getId(),PageRequest.of(0,1)).stream().findFirst().ifPresent(b -> {
            a.setWinnerId(b.getBidderId());
            if (!a.isWinnerNotified()) {
                artworks.findById(a.getArtworkId()).ifPresent(work -> notificationEvents.auctionWon(b.getBidderId(), work.getTitle()));
                a.setWinnerNotified(true);
            }
        });
    }

    private Auction auction(UUID id) { return auctions.findById(id).orElseThrow(() -> ApiException.notFound("AUCTION_NOT_FOUND", "Auction not found")); }
}
