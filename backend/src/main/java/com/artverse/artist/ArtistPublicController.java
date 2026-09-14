package com.artverse.artist;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/artists")
public class ArtistPublicController {
    private final ArtistPublicService service;

    public ArtistPublicController(ArtistPublicService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public ArtistPublicDtos.ProfileResponse profile(@PathVariable UUID userId) {
        return service.profile(userId);
    }

    @GetMapping("/{userId}/artworks")
    public Page<ArtistPublicDtos.ArtworkResponse> artworks(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        return service.artworks(userId, page, size);
    }
}
