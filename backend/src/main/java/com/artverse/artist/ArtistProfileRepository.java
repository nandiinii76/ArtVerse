package com.artverse.artist;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ArtistProfileRepository extends JpaRepository<ArtistProfile, UUID> {
    Optional<ArtistProfile> findByUserId(UUID userId);
}
