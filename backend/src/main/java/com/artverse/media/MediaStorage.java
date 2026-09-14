package com.artverse.media;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MediaStorage {
    StoredMedia store(MultipartFile file, String folder, String publicId) throws IOException;
    record StoredMedia(String storageKey, String url, String checksum) {}
}
