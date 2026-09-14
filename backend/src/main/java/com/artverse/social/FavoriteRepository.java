package com.artverse.social;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    boolean existsByIdArtworkIdAndIdUserId(UUID artworkId, UUID userId);

    @Modifying
    @Transactional
    void deleteByIdArtworkIdAndIdUserId(UUID artworkId, UUID userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO artwork_favorites (artwork_id, user_id) VALUES (:artworkId, :userId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void saveIfAbsent(@Param("artworkId") UUID artworkId, @Param("userId") UUID userId);
}
