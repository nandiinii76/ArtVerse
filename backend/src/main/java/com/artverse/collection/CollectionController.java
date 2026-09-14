package com.artverse.collection;

import com.artverse.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collections")
public class CollectionController {
    private final CollectionService service;

    public CollectionController(CollectionService service) { this.service = service; }

    @GetMapping
    public Page<CollectionDtos.Response> mine(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @AuthenticationPrincipal User user) {
        return service.myCollections(user, page, size);
    }

    @PostMapping
    public CollectionDtos.Response create(@Valid @RequestBody CollectionDtos.CreateRequest request,
                                          @AuthenticationPrincipal User user) {
        return service.create(request, user);
    }

    @GetMapping("/{id}")
    public CollectionDtos.Response get(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return service.get(id, user);
    }

    @PatchMapping("/{id}")
    public CollectionDtos.Response update(@PathVariable UUID id,
                                           @Valid @RequestBody CollectionDtos.UpdateRequest request,
                                           @AuthenticationPrincipal User user) {
        return service.update(id, request, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        service.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/artworks")
    public Page<CollectionDtos.ArtworkResponse> artworks(@PathVariable UUID id,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "24") int size,
                                                          @AuthenticationPrincipal User user) {
        return service.artworks(id, user, page, size);
    }

    @PostMapping("/{id}/artworks/{artworkId}")
    public CollectionDtos.ArtworkResponse addArtwork(@PathVariable UUID id,
                                                      @PathVariable UUID artworkId,
                                                      @AuthenticationPrincipal User user) {
        return service.addArtwork(id, artworkId, user);
    }

    @DeleteMapping("/{id}/artworks/{artworkId}")
    public ResponseEntity<Void> removeArtwork(@PathVariable UUID id,
                                               @PathVariable UUID artworkId,
                                               @AuthenticationPrincipal User user) {
        service.removeArtwork(id, artworkId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public/{id}")
    public CollectionDtos.Response publicCollection(@PathVariable UUID id) {
        return service.publicCollection(id);
    }

    @GetMapping("/public/{id}/artworks")
    public Page<CollectionDtos.ArtworkResponse> publicArtworks(@PathVariable UUID id,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "24") int size) {
        return service.publicArtworks(id, page, size);
    }
}
