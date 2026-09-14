package com.artverse.social;

import com.artverse.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social/favorites")
public class FavoriteController {
    private final FavoriteRepository repository;
    public FavoriteController(FavoriteRepository repository) { this.repository = repository; }

    @PostMapping("/{artworkId}")
    public ResponseEntity<Void> add(@PathVariable UUID artworkId, @AuthenticationPrincipal User user) {
        repository.saveIfAbsent(artworkId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{artworkId}")
    public ResponseEntity<Void> remove(@PathVariable UUID artworkId, @AuthenticationPrincipal User user) {
        repository.deleteByIdArtworkIdAndIdUserId(artworkId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{artworkId}")
    public boolean isFavorite(@PathVariable UUID artworkId, @AuthenticationPrincipal User user) {
        return repository.existsByIdArtworkIdAndIdUserId(artworkId, user.getId());
    }
}
