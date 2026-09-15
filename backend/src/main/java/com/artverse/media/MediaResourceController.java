package com.artverse.media;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
public class MediaResourceController {
    private final MediaService service;
    public MediaResourceController(MediaService service) { this.service = service; }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> get(@PathVariable UUID id) {
        MediaAsset asset = service.get(id);
        Path path = service.localPath(asset);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(asset.getContentType())).body(new FileSystemResource(path));
    }
}
