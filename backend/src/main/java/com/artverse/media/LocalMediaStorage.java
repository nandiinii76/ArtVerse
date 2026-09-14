package com.artverse.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.UUID;

@Component
public class LocalMediaStorage implements MediaStorage {
    private final Path root;

    public LocalMediaStorage(@Value("${artverse.media.storage-dir:./data/media}") String storageDir) {
        this.root = Paths.get(storageDir).toAbsolutePath().normalize();
    }

    @Override
    public StoredMedia store(MultipartFile file, String folder, String publicId) throws IOException {
        String type = file.getContentType();
        String extension = "image/png".equalsIgnoreCase(type) ? ".png" : "image/webp".equalsIgnoreCase(type) ? ".webp" : ".jpg";
        String safeFolder = StringUtils.cleanPath(folder).replace("..", "");
        String key = safeFolder + "/" + UUID.randomUUID() + extension;
        Path destination = root.resolve(key).normalize();
        if (!destination.startsWith(root)) throw new IOException("Invalid storage path");
        Files.createDirectories(destination.getParent());
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        return new StoredMedia(key, "/api/v1/media/" + publicId, sha256(destination));
    }

    private static String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
            }
            StringBuilder result = new StringBuilder();
            for (byte b : digest.digest()) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception e) {
            throw new IOException("Could not calculate checksum", e);
        }
    }

    public Path path(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) throw new IllegalArgumentException("Invalid media path");
        return path;
    }
}
