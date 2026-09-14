package com.artverse.provenance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/provenance")
@RequiredArgsConstructor
public class ProvenanceController {
    private final ProvenanceService service;

    @GetMapping("/artworks/{artworkId}")
    public ProvenanceDtos.Response get(@PathVariable UUID artworkId) {
        return service.get(artworkId);
    }

    @GetMapping(value = "/artworks/{artworkId}/certificate", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<byte[]> certificate(@PathVariable UUID artworkId) {
        byte[] body = service.certificateHtml(artworkId).getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("artverse-provenance-" + artworkId + ".html")
                .build());
        return ResponseEntity.ok().headers(headers).body(body);
    }
}
