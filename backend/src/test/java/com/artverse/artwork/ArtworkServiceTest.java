package com.artverse.artwork;

import com.artverse.notification.NotificationEventService;
import com.artverse.search.ArtworkSearchService;
import com.artverse.user.User;
import com.artverse.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtworkServiceTest {
    @Mock ArtworkRepository repository;
    @Mock UserRepository users;
    @Mock NotificationEventService notifications;
    @Mock ArtworkSearchService searchService;
    @InjectMocks ArtworkService service;

    @Test
    void createAssignsArtistAndIndexesArtwork() {
        User artist = new User();
        UUID artistId = UUID.randomUUID();
        artist.setId(artistId);
        ArtworkDtos.CreateRequest request = new ArtworkDtos.CreateRequest(
                "Monsoon Study", "A study", "image.jpg", "Landscape", "Realism", "Oil", 2026,
                null, "INR", ArtworkStatus.PUBLISHED, false);
        Artwork saved = new Artwork();
        saved.setId(UUID.randomUUID());
        saved.setArtistId(artistId);
        saved.setTitle("Monsoon Study");
        saved.setCategory("Landscape");
        saved.setStatus(ArtworkStatus.PUBLISHED);
        when(repository.save(any(Artwork.class))).thenReturn(saved);

        ArtworkDtos.Response response = service.create(request, artist);

        assertEquals("Monsoon Study", response.title());
        assertEquals(artistId, saved.getArtistId());
        verify(repository).save(any(Artwork.class));
        verify(searchService).indexSafely(saved);
        verify(notifications).newArtwork(artistId, "Monsoon Study");
    }

    @Test
    void updateRejectsArtworkOwnedByAnotherArtist() {
        UUID artworkId = UUID.randomUUID();
        Artwork artwork = new Artwork();
        artwork.setId(artworkId);
        artwork.setArtistId(UUID.randomUUID());
        User otherArtist = new User();
        otherArtist.setId(UUID.randomUUID());
        when(repository.findById(artworkId)).thenReturn(java.util.Optional.of(artwork));

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.delete(artworkId, otherArtist));
        verify(repository, never()).delete(any());
    }
}
