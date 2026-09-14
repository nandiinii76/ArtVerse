package com.artverse.curator;

import com.artverse.artwork.ArtworkDtos;
import com.artverse.artwork.ArtworkRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/curator")
public class CuratorController {
    private final ArtworkRepository artworks;

    public CuratorController(ArtworkRepository artworks) {
        this.artworks = artworks;
    }

    @GetMapping("/recommendations")
    public RecommendationResponse recommendations(@RequestParam String mood,
                                                    @RequestParam(defaultValue = "6") int limit) {
        String q = normalizeMood(mood);
        int safeLimit = Math.min(Math.max(limit, 1), 12);
        List<ArtworkDtos.Response> results = artworks
                .search(q, null, com.artverse.artwork.ArtworkStatus.PUBLISHED,
                        PageRequest.of(0, safeLimit))
                .map(ArtworkDtos.Response::from)
                .getContent();
        return new RecommendationResponse(mood, explanation(mood), results);
    }

    private String normalizeMood(String mood) {
        if (mood == null) return "";
        String value = mood.trim().toLowerCase();
        if (value.contains("calm") || value.contains("peace")) return "minimal serene landscape blue nature";
        if (value.contains("dark") || value.contains("mystery")) return "dark gothic dramatic night shadow";
        if (value.contains("joy") || value.contains("happy")) return "bright vibrant colorful floral";
        if (value.contains("nostalgia") || value.contains("vintage")) return "vintage classical portrait antique";
        if (value.contains("energy") || value.contains("bold")) return "abstract modern vibrant geometric";
        return value;
    }

    private String explanation(String mood) {
        return "A studio-style recommendation based on the mood words you entered. " +
                "ArtVerse can later replace this rule-based layer with an embedding/vector model without changing the API.";
    }

    public record RecommendationResponse(String mood, String explanation, List<ArtworkDtos.Response> artworks) {}
}
