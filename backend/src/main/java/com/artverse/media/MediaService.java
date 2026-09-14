package com.artverse.media;

import com.artverse.artwork.Artwork;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
public class MediaService {
    private static final long MAX_BYTES = 10 * 1024 * 1024;
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private final MediaAssetRepository assets;
    private final ArtworkRepository artworks;
    private final Path root;

    public MediaService(MediaAssetRepository assets, ArtworkRepository artworks,
                        @Value("${artverse.media.storage-dir:./data/media}") String storageDir) {
        this.assets = assets;
        this.artworks = artworks;
        this.root = Paths.get(storageDir).toAbsolutePath().normalize();
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
        String extension = contentType.equalsIgnoreCase("image/png") ? ".png" : contentType.equalsIgnoreCase("image/webp") ? ".webp" : ".jpg";
        String key = artworkId + "/" + id + extension;
        Path destination = root.resolve(key).normalize();
        try {
            if (!destination.startsWith(root)) throw new ResponseStatusException(BAD_REQUEST, "Invalid storage path");
            Files.createDirectories(destination.getParent());
            try (InputStream input = file.getInputStream()) { Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING); }
            MediaAsset asset = new MediaAsset();
            asset.setId(id); asset.setArtworkId(artworkId); asset.setOwnerId(user.getId());
            asset.setOriginalName(original); asset.setStorageKey(key); asset.setContentType(contentType);
            asset.setFileSize(file.getSize()); asset.setChecksum(sha256(destination));
            assets.save(asset);
            artwork.setImageUrl("/api/v1/media/" + id);
            artworks.save(artwork);
            return MediaDtos.Response.from(asset);
        } catch (IOException e) {
            try { Files.deleteIfExists(destination); } catch (IOException ignored) {}
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not store image", e);
        }
    }

    public MediaAsset get(UUID id) { return assets.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Media not found")); }
    public byte[] read(MediaAsset asset) {
        Path path = root.resolve(asset.getStorageKey()).normalize();
        if (!path.startsWith(root)) throw new ResponseStatusException(BAD_REQUEST, "Invalid media path");
        try { return Files.readAllBytes(path); } catch (IOException e) { throw new ResponseStatusException(NOT_FOUND, "Media file not found", e); }
    }
    public MediaType mediaType(MediaAsset asset) { return MediaType.parseMediaType(asset.getContentType()); }

    private static String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192]; int read;
                while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
            }
            StringBuilder result = new StringBuilder();
            for (byte b : digest.digest()) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception e) { throw new IOException("Could not calculate checksum", e); }
    }
}
