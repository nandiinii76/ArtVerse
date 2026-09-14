package com.artverse.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;

@Component
public class CloudinaryMediaStorage implements MediaStorage {
    private final Cloudinary cloudinary;
    private final boolean enabled;

    public CloudinaryMediaStorage(
            @Value("${CLOUDINARY_URL:}") String cloudinaryUrl,
            @Value("${artverse.media.provider:local}") String provider) {
        this.enabled = "cloudinary".equalsIgnoreCase(provider) && !cloudinaryUrl.isBlank();
        this.cloudinary = new Cloudinary();
        if (this.enabled) this.cloudinary.configFromURL(cloudinaryUrl);
    }

    @Override
    public StoredMedia store(MultipartFile file, String folder, String publicId) throws IOException {
        if (!enabled) throw new IOException("Cloudinary storage is not configured");
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", publicId,
                    "resource_type", "image",
                    "overwrite", false,
                    "use_filename", false,
                    "unique_filename", false));
            return new StoredMedia(String.valueOf(result.get("public_id")), String.valueOf(result.get("secure_url")), sha256(file.getBytes()));
        } catch (Exception e) {
            throw new IOException("Could not upload image to Cloudinary", e);
        }
    }

    private static String sha256(byte[] bytes) throws IOException {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder result = new StringBuilder();
            for (byte b : digest) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception e) {
            throw new IOException("Could not calculate checksum", e);
        }
    }
}
