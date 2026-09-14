package com.artverse.collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {
    Page<Collection> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);
}
