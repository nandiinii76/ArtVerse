package com.artverse.media;

import com.artverse.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}
