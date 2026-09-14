package com.artverse.media;

import com.artverse.user.User;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {
    private final MediaService service;
    public MediaController(MediaService service) { this.service = service; }

    @PostMapping("/artworks/{artworkId}")
    public MediaDtos.Response upload(@PathVariable UUID artworkId, @RequestParam MultipartFile file,
                                     @AuthenticationPrincipal User user) {
        return service.upload(artworkId, file, user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ByteArrayResource> get(@PathVariable UUID id) throws Exception {
        MediaAsset asset = service.get(id);
        byte[] bytes = Files.readAllBytes(service.localPath(asset));
        return ResponseEntity.ok().header("Content-Type", asset.getContentType()).contentLength(bytes.length).body(new ByteArrayResource(bytes));
    }
}
