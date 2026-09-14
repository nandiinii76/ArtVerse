package com.artverse.collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CollectionArtworkRepository extends JpaRepository<CollectionArtwork, CollectionArtworkId> {
    Page<CollectionArtwork> findByIdCollectionIdOrderByAddedAtDesc(UUID collectionId, Pageable pageable);
    long countByIdCollectionId(UUID collectionId);
}
