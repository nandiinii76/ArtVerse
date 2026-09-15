package com.artverse.admin;

import com.artverse.artwork.Artwork;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.artwork.ArtworkStatus;
import com.artverse.user.Role;
import com.artverse.user.User;
import com.artverse.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    @Mock UserRepository users;
    @Mock ArtworkRepository artworks;
    @InjectMocks AdminController controller;

    @Test
    void statsCountsArtists() {
        User artist = new User();
        artist.setRoles(EnumSet.of(Role.USER, Role.ARTIST));
        User member = new User();
        member.setRoles(EnumSet.of(Role.USER));
        when(users.count()).thenReturn(2L);
        when(users.findAll()).thenReturn(java.util.List.of(artist, member));
        when(artworks.count()).thenReturn(5L);

        AdminDtos.PlatformStats stats = controller.stats();

        assertEquals(2, stats.users());
        assertEquals(1, stats.artists());
        assertEquals(5, stats.artworks());
    }

    @Test
    void disablingUserPersistsNewState() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setDisplayName("Gallery Member");
        user.setEmail("member@example.com");
        user.setEnabled(true);
        when(users.findById(id)).thenReturn(java.util.Optional.of(user));
        when(users.save(user)).thenReturn(user);

        AdminDtos.UserSummary result = controller.updateUserEnabled(id, new AdminDtos.ToggleRequest(false));

        assertEquals(false, result.enabled());
        verify(users).save(user);
    }

    @Test
    void featuringArtworkPersistsNewState() {
        UUID id = UUID.randomUUID();
        Artwork artwork = new Artwork();
        artwork.setId(id);
        artwork.setTitle("The Blue Hour");
        artwork.setArtistId(UUID.randomUUID());
        artwork.setCategory("Landscape");
        artwork.setStatus(ArtworkStatus.PUBLISHED);
        artwork.setFeatured(false);
        when(artworks.findById(id)).thenReturn(java.util.Optional.of(artwork));
        when(artworks.save(artwork)).thenReturn(artwork);

        AdminDtos.ArtworkSummary result = controller.updateArtworkFeatured(id, new AdminDtos.FeatureRequest(true));

        assertEquals(true, result.featured());
        verify(artworks).save(artwork);
    }
}
