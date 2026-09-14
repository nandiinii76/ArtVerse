package com.artverse.artwork;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "artworks", indexes = {
        @Index(name = "idx_artwork_category", columnList = "category"),
        @Index(name = "idx_artwork_artist", columnList = "artist_id"),
        @Index(name = "idx_artwork_title", columnList = "title")
})
@Getter @Setter @NoArgsConstructor
public class Artwork {
    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 5000)
    private String description;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "artist_id", nullable = false)
    private UUID artistId;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(length = 100)
    private String style;

    @Column(length = 120)
    private String medium;

    @Column(name = "year_created")
    private Integer yearCreated;

    @Column(precision = 14, scale = 2)
    private BigDecimal price;

    @Column(length = 10)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArtworkStatus status = ArtworkStatus.PUBLISHED;

    @Column(nullable = false)
    private boolean featured;

    @Column(nullable = false)
    private long views;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist void create() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void update() { updatedAt = Instant.now(); }
}
