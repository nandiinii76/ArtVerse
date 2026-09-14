package com.artverse.social;

import com.artverse.artwork.Artwork;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.notification.NotificationEventService;
import com.artverse.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialNotificationService {
    private final ArtworkRepository artworks;
    private final NotificationEventService events;

    public void favorite(UUID artworkId, User actor) {
        artworks.findById(artworkId).ifPresent(a -> events.favorite(a.getArtistId(), actor.getId(), a.getTitle()));
    }

    public void comment(UUID artworkId, User actor) {
        artworks.findById(artworkId).ifPresent(a -> events.comment(a.getArtistId(), actor.getId(), a.getTitle()));
    }
}
