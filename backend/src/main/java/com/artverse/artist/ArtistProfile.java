package com.artverse.artist;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "artist_profiles")
@Getter
@Setter
@NoArgsConstructor
public class ArtistProfile {
    @Id @GeneratedValue private UUID id;
    @Column(name = "user_id", nullable = false, unique = true) private UUID userId;
    @Column(length = 5000) private String biography;
    @Column(length = 180) private String location;
    @Column(name = "website_url", length = 500) private String websiteUrl;
    @Column(name = "profile_image_url", length = 1000) private String profileImageUrl;
    @Column(nullable = false) private boolean verified;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    @PrePersist void create() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void update() { updatedAt = Instant.now(); }
}
