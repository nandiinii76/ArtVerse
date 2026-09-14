package com.artverse.artwork;

import com.artverse.user.User;
import com.artverse.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Service
public class ArtworkService {
    private final ArtworkRepository repository;
    private final UserRepository users;

    public ArtworkService(ArtworkRepository repository, UserRepository users) {
        this.repository = repository;
        this.users = users;
    }

    public ArtworkDtos.Response create(ArtworkDtos.CreateRequest request, User user) {
        Artwork a = new Artwork();
        apply(a, request);
        a.setArtistId(user.getId());
        return ArtworkDtos.Response.from(repository.save(a));
    }

    public Page<ArtworkDtos.Response> search(String q, String category, Pageable pageable) {
        return repository.search(blankToNull(q), blankToNull(category), ArtworkStatus.PUBLISHED, pageable).map(ArtworkDtos.Response::from);
    }

    public ArtworkDtos.Response get(UUID id) {
        Artwork a = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artwork not found"));
        if (a.getStatus() == ArtworkStatus.PUBLISHED) {
            a.setViews(a.getViews() + 1);
            repository.save(a);
        }
        return ArtworkDtos.Response.from(a);
    }

    public ArtworkDtos.Response update(UUID id, ArtworkDtos.CreateRequest request, User user) {
        Artwork a = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artwork not found"));
        if (!a.getArtistId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the artist can edit this artwork");
        apply(a, request);
        return ArtworkDtos.Response.from(repository.save(a));
    }

    public void delete(UUID id, User user) {
        Artwork a = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artwork not found"));
        if (!a.getArtistId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the artist can delete this artwork");
        repository.delete(a);
    }

    private void apply(Artwork a, ArtworkDtos.CreateRequest r) {
        a.setTitle(r.title()); a.setDescription(r.description()); a.setImageUrl(r.imageUrl());
        a.setCategory(r.category()); a.setStyle(r.style()); a.setMedium(r.medium()); a.setYearCreated(r.yearCreated());
        a.setPrice(r.price()); a.setCurrency(r.currency() == null || r.currency().isBlank() ? "INR" : r.currency());
        if (r.status() != null) a.setStatus(r.status());
        if (r.featured() != null) a.setFeatured(r.featured());
    }

    private String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
