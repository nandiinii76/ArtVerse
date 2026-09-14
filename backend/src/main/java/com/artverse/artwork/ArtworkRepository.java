package com.artverse.artwork;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ArtworkRepository extends JpaRepository<Artwork, UUID> {
    Page<Artwork> findByStatus(ArtworkStatus status, Pageable pageable);
    Page<Artwork> findByArtistIdAndStatus(UUID artistId, ArtworkStatus status, Pageable pageable);

    @Query("select a from Artwork a where a.status = :status and " +
           "(:q is null or lower(a.title) like lower(concat('%', :q, '%')) " +
           "or lower(coalesce(a.description,'')) like lower(concat('%', :q, '%')) " +
           "or lower(coalesce(a.style,'')) like lower(concat('%', :q, '%')) " +
           "or lower(coalesce(a.medium,'')) like lower(concat('%', :q, '%'))) and " +
           "(:category is null or lower(a.category) = lower(:category))")
    Page<Artwork> search(@Param("q") String q, @Param("category") String category,
                         @Param("status") ArtworkStatus status, Pageable pageable);
}
