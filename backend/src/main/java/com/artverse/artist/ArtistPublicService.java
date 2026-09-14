package com.artverse.artist;

import com.artverse.artwork.Artwork;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.artwork.ArtworkStatus;
import com.artverse.common.ApiException;
import com.artverse.social.FollowRepository;
import com.artverse.user.User;
import com.artverse.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistPublicService {
    private final ArtistProfileRepository profiles;
    private final UserRepository users;
    private final ArtworkRepository artworks;
    private final FollowRepository follows;

    public ArtistPublicDtos.ProfileResponse profile(UUID userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> ApiException.notFound("ARTIST_NOT_FOUND", "Artist not found"));
        ArtistProfile profile = profiles.findByUserId(userId).orElse(null);
        long artworkCount = artworks.countByArtistIdAndStatus(userId, ArtworkStatus.PUBLISHED);
        long followerCount = follows.countByIdFollowingId(userId);
        return new ArtistPublicDtos.ProfileResponse(
                user.getId(), user.getDisplayName(),
                profile == null ? user.getBio() : profile.getBiography(),
                profile == null ? null : profile.getLocation(),
                profile == null ? null : profile.getWebsiteUrl(),
                profile == null ? user.getAvatarUrl() : profile.getProfileImageUrl(),
                profile != null && profile.isVerified(),
                user.getCreatedAt(), artworkCount, followerCount);
    }

    public Page<ArtistPublicDtos.ArtworkResponse> artworks(UUID userId, int page, int size) {
        users.findById(userId).orElseThrow(() -> ApiException.notFound("ARTIST_NOT_FOUND", "Artist not found"));
        return artworks.findByArtistIdAndStatus(userId, ArtworkStatus.PUBLISHED,
                        PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 48)))
                .map(this::toResponse);
    }

    private ArtistPublicDtos.ArtworkResponse toResponse(Artwork a) {
        return new ArtistPublicDtos.ArtworkResponse(a.getId(), a.getTitle(), a.getDescription(),
                a.getImageUrl(), a.getCategory(), a.getMedium(), a.getStyle(), a.getYearCreated(),
                a.getStatus().name(), a.getPrice(), a.getCurrency());
    }
}
