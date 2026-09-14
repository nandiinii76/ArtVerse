package com.artverse.auction;

import com.artverse.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auctions")
public class AuctionController {
    private final AuctionService service;
    public AuctionController(AuctionService service) { this.service = service; }

    @GetMapping public Page<AuctionDtos.Response> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return service.list(page,size); }
    @GetMapping("/mine") public Page<AuctionDtos.Response> mine(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="30") int size, @AuthenticationPrincipal User user) { return service.myAuctions(user,page,size); }
    @GetMapping("/{id}") public AuctionDtos.Response get(@PathVariable UUID id) { return service.get(id); }
    @GetMapping("/{id}/bids") public Page<AuctionDtos.BidResponse> bids(@PathVariable UUID id, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="30") int size) { return service.bids(id,page,size); }
    @PostMapping("/artworks/{artworkId}") public AuctionDtos.Response create(@PathVariable UUID artworkId, @Valid @RequestBody AuctionDtos.CreateRequest request, @AuthenticationPrincipal User user) { return service.create(artworkId,request,user); }
    @PostMapping("/{id}/bids") public AuctionDtos.BidResponse bid(@PathVariable UUID id, @Valid @RequestBody AuctionDtos.BidRequest request, @AuthenticationPrincipal User user) { return service.bid(id,request,user); }
    @PostMapping("/{id}/close") public AuctionDtos.Response close(@PathVariable UUID id, @AuthenticationPrincipal User user) { return service.close(id,user); }
}
