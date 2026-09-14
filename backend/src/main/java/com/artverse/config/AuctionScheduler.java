package com.artverse.config;

import com.artverse.auction.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionScheduler {
    private final AuctionService auctionService;

    @Scheduled(fixedDelay = 15000)
    public void synchronizeAuctions() {
        auctionService.syncDueAuctions();
    }
}
