package com.artverse.collection;

import com.artverse.artwork.Artwork;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.artwork.ArtworkStatus;
import com.artverse.common.ApiException;
import com.artverse.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionService {
    private final CollectionRepository collections;
    private final CollectionArtworkRepository items;
    private final ArtworkRepository artworks;

    public Page<CollectionDtos.Response> myCollections(User user, int page, int size) {
        return collections.findByOwnerIdOrderByCreatedAtDesc(user.getId(), pageRequest(page, size)).map(this::response);
    }

    @Transactional
    public CollectionDtos.Response create(CollectionDtos.CreateRequest request, User user) {
        Collection collection = new Collection();
        collection.setOwnerId(user.getId());
        collection.setName(request.name().trim());
        collection.setDescription(request.description());
        collection.setPublicCollection(request.publicCollection() == null || request.publicCollection());
        return response(collections.save(collection));
    }

    public CollectionDtos.Response get(UUID id, User user) {
        Collection c = collection(id);
        ensureOwner(c, user);
        return response(c);
    }

    @Transactional
    public CollectionDtos.Response update(UUID id, CollectionDtos.UpdateRequest request, User user) {
        Collection c = collection(id);
        ensureOwner(c, user);
        c.setName(request.name().trim());
        c.setDescription(request.description());
        if (request.publicCollection() != null) c.setPublicCollection(request.publicCollection());
        if (request.coverArtworkId() != null) {
            if (!items.existsById(new CollectionArtworkId(id, request.coverArtworkId())))
                throw ApiException.notFound("ARTWORK_NOT_IN_COLLECTION", "Cover artwork must belong to this collection");
            c.setCoverArtworkId(request.coverArtworkId());
        } else c.setCoverArtworkId(null);
        return response(collections.save(c));
    }

    @Transactional
    public void delete(UUID id, User user) {
        Collection c = collection(id);
        ensureOwner(c, user);
        collections.delete(c);
    }

    @Transactional
    public CollectionDtos.ArtworkResponse addArtwork(UUID id, UUID artworkId, User user) {
        Collection c = collection(id);
        ensureOwner(c, user);
        Artwork artwork = artworks.findById(artworkId).orElseThrow(() -> ApiException.notFound("ARTWORK_NOT_FOUND", "Artwork not found"));
        if (artwork.getStatus() != ArtworkStatus.PUBLISHED && !artwork.getArtistId().equals(user.getId()))
            throw ApiException.notFound("ARTWORK_NOT_FOUND", "Artwork not available");
        CollectionArtworkId key = new CollectionArtworkId(id, artworkId);
        if (!items.existsById(key)) items.save(new CollectionArtwork(key));
        CollectionArtwork item = items.findById(key).orElseThrow();
        return new CollectionDtos.ArtworkResponse(id, artworkId, item.getAddedAt());
    }

    @Transactional
    public void removeArtwork(UUID id, UUID artworkId, User user) {
        Collection c = collection(id);
        ensureOwner(c, user);
        items.deleteById(new CollectionArtworkId(id, artworkId));
        if (artworkId.equals(c.getCoverArtworkId())) c.setCoverArtworkId(null);
    }

    public Page<CollectionDtos.ArtworkResponse> artworks(UUID id, User user, int page, int size) {
        Collection c = collection(id);
        ensureOwner(c, user);
        return items.findByIdCollectionIdOrderByAddedAtDesc(id, pageRequest(page, size))
                .map(item -> new CollectionDtos.ArtworkResponse(id, item.getId().getArtworkId(), item.getAddedAt()));
    }

    public CollectionDtos.Response publicCollection(UUID id) {
        Collection c = collection(id);
        if (!c.isPublicCollection()) throw new ApiException(HttpStatus.NOT_FOUND, "COLLECTION_NOT_FOUND", "Collection not found");
        return response(c);
    }

    public Page<CollectionDtos.ArtworkResponse> publicArtworks(UUID id, int page, int size) {
        Collection c = collection(id);
        if (!c.isPublicCollection()) throw new ApiException(HttpStatus.NOT_FOUND, "COLLECTION_NOT_FOUND", "Collection not found");
        return items.findByIdCollectionIdOrderByAddedAtDesc(id, pageRequest(page, size))
                .map(item -> new CollectionDtos.ArtworkResponse(id, item.getId().getArtworkId(), item.getAddedAt()));
    }

    private CollectionDtos.Response response(Collection c) {
        return new CollectionDtos.Response(c.getId(), c.getOwnerId(), c.getName(), c.getDescription(), c.getCoverArtworkId(), c.isPublicCollection(), items.countByIdCollectionId(c.getId()), c.getCreatedAt());
    }

    private Collection collection(UUID id) {
        return collections.findById(id).orElseThrow(() -> ApiException.notFound("COLLECTION_NOT_FOUND", "Collection not found"));
    }

    private void ensureOwner(Collection c, User user) {
        if (!c.getOwnerId().equals(user.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "NOT_OWNER", "Only the collection owner can modify it");
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
    }
}
