package com.artverse.artwork;

import com.artverse.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/artworks")
public class ArtworkController {
    private final ArtworkService service;
    public ArtworkController(ArtworkService service) { this.service = service; }

    @GetMapping
    public Page<ArtworkDtos.Response> search(@RequestParam(required=false) String q,
                                             @RequestParam(required=false) String category,
                                             @RequestParam(defaultValue="0") int page,
                                             @RequestParam(defaultValue="12") int size,
                                             @RequestParam(defaultValue="createdAt") String sort,
                                             @RequestParam(defaultValue="desc") String direction) {
        size = Math.min(Math.max(size, 1), 50);
        Sort s = Sort.by("asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC, sort);
        return service.search(q, category, PageRequest.of(Math.max(page, 0), size, s));
    }

    @GetMapping("/mine")
    public Page<ArtworkDtos.Response> mine(@RequestParam(defaultValue="0") int page,
                                           @RequestParam(defaultValue="24") int size,
                                           @AuthenticationPrincipal User user) {
        size = Math.min(Math.max(size, 1), 50);
        return service.mine(user.getId(), PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/{id}") public ArtworkDtos.Response get(@PathVariable UUID id) { return service.get(id); }

    @PostMapping
    public ArtworkDtos.Response create(@Valid @RequestBody ArtworkDtos.CreateRequest request, @AuthenticationPrincipal User user) {
        return service.create(request, user);
    }

    @PutMapping("/{id}")
    public ArtworkDtos.Response update(@PathVariable UUID id, @Valid @RequestBody ArtworkDtos.CreateRequest request,
                                       @AuthenticationPrincipal User user) { return service.update(id, request, user); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        service.delete(id, user); return ResponseEntity.noContent().build();
    }
}
