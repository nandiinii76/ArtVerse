package com.artverse.social;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    boolean existsByIdFollowerIdAndIdFollowingId(UUID followerId, UUID followingId);
    Page<Follow> findByIdFollowingId(UUID followingId, Pageable pageable);
    Page<Follow> findByIdFollowerId(UUID followerId, Pageable pageable);
    long countByIdFollowingId(UUID followingId);
    long countByIdFollowerId(UUID followerId);
}
