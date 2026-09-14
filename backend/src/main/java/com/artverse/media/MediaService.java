package com.artverse.media;

import com.artverse.artwork.Artwork;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
public class MediaService {
    private static final long MAX_BYTES = 10 * 1024 * 1024;
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private final MediaAssetRepository assets;
    private final ArtworkRepository artworks;
    private final LocalMediaStorage localStorage;
    private final CloudinaryMediaStorage cloudinaryStorage;
    private final String provider;

    public MediaService(MediaAssetRepository assets, ArtworkRepository artworks, LocalMediaStorage localStorage,
                        CloudinaryMediaStorage cloudinaryStorage, @Value("${artverse.media.provider:local}") String provider) {
        this.assets = assets; this.artworks = artworks; this.localStorage = localStorage; this.cloudinaryStorage = cloudinaryStorage;
        this.provider = provider.toLowerCase();
    }

    public MediaDtos.Response upload(UUID artworkId, MultipartFile file, User user) {
        Artwork artwork = artworks.findById(artworkId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Artwork not found"));
        if (!artwork.getArtistId().equals(user.getId())) throw new ResponseStatusException(FORBIDDEN, "Only the artist can upload media for this artwork");
        if (file == null || file.isEmpty()) throw new ResponseStatusException(BAD_REQUEST, "Please choose an image");
        if (file.getSize() > MAX_BYTES) throw new ResponseStatusException(PAYLOAD_TOO_LARGE, "Image must be 10 MB or smaller");
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) throw new ResponseStatusException(UNSUPPORTED_MEDIA_TYPE, "Only JPEG, PNG, and WebP images are supported");
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "artwork" : file.getOriginalFilename());
        if (original.contains("..")) throw new ResponseStatusException(BAD_REQUEST, "Invalid filename");
        UUID id = UUID.randomUUID();
        try {
            MediaStorage storage = "cloudinary".equals(provider) ? cloudinaryStorage : localStorage;
            MediaStorage.StoredMedia stored = storage.store(file, "artverse/artworks/" + artworkId, id.toString());
            MediaAsset asset = new MediaAsset();
            asset.setId(id); asset.setArtworkId(artworkId); asset.setOwnerId(user.getId());
            asset.setOriginalName(original); asset.setStorageKey(stored.storageKey()); asset.setProvider("cloudinary".equals(provider) ? "cloudinary" : "local");
            asset.setResourceUrl(stored.url()); asset.setContentType(contentType); asset.setFileSize(file.getSize()); asset.setChecksum(stored.checksum());
            assets.save(asset);
            artwork.setImageUrl(stored.url()); artworks.save(artwork);
            return MediaDtos.Response.from(asset);
        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not store image", e);
        }
    }

    public MediaAsset get(UUID id) { return assets.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Media not found")); }
    public java.nio.file.Path localPath(MediaAsset asset) {
        if (!"local".equals(asset.getProvider())) throw new ResponseStatusException(NOT_FOUND, "Cloud media is served directly by its provider");
        return localStorage.path(asset.getStorageKey());
    }
}
