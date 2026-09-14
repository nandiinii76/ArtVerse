package com.artverse.artist;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/artists")
public class ArtistDirectoryController {
    private final ArtistDirectoryService service;

    public ArtistDirectoryController(ArtistDirectoryService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ArtistDirectoryDtos.Response> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        return service.search(q, location, verified, sort, page, size);
    }
}
