package com.artverse.admin;

import com.artverse.artwork.ArtworkRepository;
import com.artverse.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository users;
    private final ArtworkRepository artworks;

    @GetMapping("/stats")
    public AdminDtos.PlatformStats stats() {
        long artists = users.findAll().stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.name().equals("ARTIST")))
                .count();
        return new AdminDtos.PlatformStats(users.count(), artists, artworks.count(), 0, 0);
    }

    @GetMapping("/users")
    public Page<AdminDtos.UserSummary> users(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return users.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))))
                .map(AdminDtos.UserSummary::from);
    }

    @GetMapping("/artworks")
    public Page<AdminDtos.ArtworkSummary> artworks(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return artworks.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))))
                .map(AdminDtos.ArtworkSummary::from);
    }
}
