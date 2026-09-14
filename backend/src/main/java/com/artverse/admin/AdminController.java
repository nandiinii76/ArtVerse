package com.artverse.admin;

import com.artverse.artwork.Artwork;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.user.User;
import com.artverse.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository users;
    private final ArtworkRepository artworks;

    @GetMapping("/stats")
    public AdminDtos.PlatformStats stats() {

        long artists = users.findAll()
                .stream()
                .filter(user ->
                        user.getRoles()
                                .stream()
                                .anyMatch(role -> role.name().equals("ARTIST")))
                .count();

        return new AdminDtos.PlatformStats(
                users.count(),
                artists,
                artworks.count(),
                0,
                0
        );
    }

    @GetMapping("/users")
    public Page<AdminDtos.UserSummary> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));

        return users
                .findAll(PageRequest.of(safePage, safeSize))
                .map(AdminDtos.UserSummary::from);
    }

    @PatchMapping("/users/{id}/enabled")
    public AdminDtos.UserSummary updateUserEnabled(
            @PathVariable UUID id,
            @RequestBody AdminDtos.ToggleRequest request
    ) {

        User user = users.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        user.setEnabled(request.enabled());

        User saved = users.save(user);

        return AdminDtos.UserSummary.from(saved);
    }

    @GetMapping("/artworks")
    public Page<AdminDtos.ArtworkSummary> artworks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));

        return artworks
                .findAll(PageRequest.of(safePage, safeSize))
                .map(AdminDtos.ArtworkSummary::from);
    }

    @PatchMapping("/artworks/{id}/featured")
    public AdminDtos.ArtworkSummary updateArtworkFeatured(
            @PathVariable UUID id,
            @RequestBody AdminDtos.FeatureRequest request
    ) {

        Artwork artwork = artworks.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Artwork not found"
                        )
                );

        artwork.setFeatured(request.featured());

        Artwork saved = artworks.save(artwork);

        return AdminDtos.ArtworkSummary.from(saved);
    }
}
