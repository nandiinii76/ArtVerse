package com.artverse.artist;

import com.artverse.user.User;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/artists")
public class ArtistController {
    private final ArtistProfileRepository repository;
    public ArtistController(ArtistProfileRepository repository) { this.repository = repository; }

    @GetMapping("/{userId}") public ArtistProfile get(@PathVariable UUID userId) {
        return repository.findByUserId(userId).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Artist profile not found"));
    }

    @PutMapping("/me") public ArtistProfile update(@AuthenticationPrincipal User user, @RequestBody UpdateRequest r) {
        ArtistProfile p = repository.findByUserId(user.getId()).orElseGet(() -> { ArtistProfile x=new ArtistProfile(); x.setUserId(user.getId()); return x; });
        p.setBiography(r.biography()); p.setLocation(r.location()); p.setWebsiteUrl(r.websiteUrl()); p.setProfileImageUrl(r.profileImageUrl());
        return repository.save(p);
    }

    public record UpdateRequest(@Size(max=5000) String biography, @Size(max=180) String location,
                                @Size(max=500) String websiteUrl, @Size(max=1000) String profileImageUrl) {}
}
