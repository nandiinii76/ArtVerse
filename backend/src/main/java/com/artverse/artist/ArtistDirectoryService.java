package com.artverse.artist;

import com.artverse.artwork.ArtworkRepository;
import com.artverse.artwork.ArtworkStatus;
import com.artverse.common.ApiException;
import com.artverse.social.FollowRepository;
import com.artverse.user.Role;
import com.artverse.user.User;
import com.artverse.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ArtistDirectoryService {
    private final UserRepository users;
    private final ArtistProfileRepository profiles;
    private final ArtworkRepository artworks;
    private final FollowRepository follows;

    public Page<ArtistDirectoryDtos.Response> search(String q, String location, Boolean verified, String sort, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 48);
        String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        String place = location == null ? "" : location.trim().toLowerCase(Locale.ROOT);

        List<User> artistUsers = users.findAll(Sort.by(Sort.Direction.ASC, "displayName")).stream()
                .filter(user -> user.getRoles().contains(Role.ARTIST))
                .filter(user -> query.isBlank()
                        || contains(user.getDisplayName(), query)
                        || contains(user.getBio(), query))
                .filter(user -> place.isBlank() || profiles.findByUserId(user.getId())
                        .map(profile -> contains(profile.getLocation(), place))
                        .orElse(false))
                .filter(user -> verified == null || profiles.findByUserId(user.getId())
                        .map(ArtistProfile::isVerified).orElse(false) == verified)
                .toList();

        List<ArtistDirectoryDtos.Response> results = new ArrayList<>(artistUsers.stream().map(this::toResponse).toList());
        Comparator<ArtistDirectoryDtos.Response> comparator = Comparator.comparing(ArtistDirectoryDtos.Response::displayName, String.CASE_INSENSITIVE_ORDER);
        if ("followers".equalsIgnoreCase(sort)) comparator = Comparator.comparingLong(ArtistDirectoryDtos.Response::followerCount).reversed().thenComparing(comparator);
        if ("artworks".equalsIgnoreCase(sort)) comparator = Comparator.comparingLong(ArtistDirectoryDtos.Response::artworkCount).reversed().thenComparing(comparator);
        if ("newest".equalsIgnoreCase(sort)) comparator = Comparator.comparing(ArtistDirectoryDtos.Response::joinedAt).reversed();
        results.sort(comparator);

        int from = Math.min(safePage * safeSize, results.size());
        int to = Math.min(from + safeSize, results.size());
        return new PageImpl<>(results.subList(from, to), PageRequest.of(safePage, safeSize), results.size());
    }

    private ArtistDirectoryDtos.Response toResponse(User user) {
        ArtistProfile profile = profiles.findByUserId(user.getId()).orElse(null);
        return new ArtistDirectoryDtos.Response(
                user.getId(), user.getDisplayName(), profile == null ? user.getBio() : profile.getBiography(),
                profile == null ? null : profile.getLocation(), profile == null ? null : profile.getWebsiteUrl(),
                profile == null ? user.getAvatarUrl() : profile.getProfileImageUrl(),
                profile != null && profile.isVerified(), user.getCreatedAt(),
                artworks.countByArtistIdAndStatus(user.getId(), ArtworkStatus.PUBLISHED),
                follows.countByIdFollowingId(user.getId()));
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
