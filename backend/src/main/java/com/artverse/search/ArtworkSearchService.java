package com.artverse.search;

import com.artverse.artwork.Artwork;
import com.artverse.artwork.ArtworkDtos;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.artwork.ArtworkStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ArtworkSearchService {

    private static final String INDEX = "artverse-artworks";

    private final ArtworkRepository artworks;
    private final ObjectMapper objectMapper;
    private final RestClient client;
    private final boolean enabled;

    public ArtworkSearchService(
            ArtworkRepository artworks,
            ObjectMapper objectMapper,
            @Value("${artverse.search.opensearch-url:http://localhost:9200}") String url,
            @Value("${artverse.search.opensearch-enabled:true}") boolean enabled) {
        this.artworks = artworks;
        this.objectMapper = objectMapper;
        this.client = RestClient.builder().baseUrl(url).build();
        this.enabled = enabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildIndex() {
        if (!enabled) return;
        try {
            client.put().uri("/" + INDEX).body(""
                    + "{\"settings\":{\"number_of_shards\":1,\"number_of_replicas\":0},"
                    + "\"mappings\":{\"properties\":{"
                    + "\"id\":{\"type\":\"keyword\"},"
                    + "\"title\":{\"type\":\"text\"},"
                    + "\"description\":{\"type\":\"text\"},"
                    + "\"category\":{\"type\":\"keyword\"},"
                    + "\"style\":{\"type\":\"text\"},"
                    + "\"medium\":{\"type\":\"text\"},"
                    + "\"status\":{\"type\":\"keyword\"},"
                    + "\"featured\":{\"type\":\"boolean\"},"
                    + "\"yearCreated\":{\"type\":\"integer\"},"
                    + "\"createdAt\":{\"type\":\"date\"}"
                    + "}}}")
                    .header("Content-Type", "application/json")
                    .retrieve().toBodilessEntity();
        } catch (Exception ignored) {
            // The index may already exist. The application must remain usable without OpenSearch.
        }

        artworks.findByStatus(ArtworkStatus.PUBLISHED, Pageable.unpaged())
                .forEach(this::indexSafely);
    }

    public Page<ArtworkDtos.Response> search(String q, String category, Pageable pageable) {
        if (!enabled || (q == null && category == null)) return null;

        try {
            String body = buildQuery(q, category, pageable);
            String response = client.post().uri("/" + INDEX + "/_search")
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            List<UUID> ids = new ArrayList<>();
            JsonNode hits = root.path("hits").path("hits");
            for (JsonNode hit : hits) {
                String id = hit.path("_id").asText();
                if (!id.isBlank()) ids.add(UUID.fromString(id));
            }

            long total = root.path("hits").path("total").path("value").asLong(ids.size());
            List<Artwork> found = artworks.findAllById(ids);
            Map<UUID, Artwork> byId = new HashMap<>();
            found.forEach(a -> byId.put(a.getId(), a));

            List<ArtworkDtos.Response> ordered = ids.stream()
                    .map(byId::get)
                    .filter(a -> a != null && a.getStatus() == ArtworkStatus.PUBLISHED)
                    .map(ArtworkDtos.Response::from)
                    .toList();

            return new PageImpl<>(ordered, pageable, total);
        } catch (Exception ignored) {
            return null;
        }
    }

    public void indexSafely(Artwork artwork) {
        if (!enabled) return;
        try {
            if (artwork.getStatus() != ArtworkStatus.PUBLISHED) {
                delete(artwork.getId());
                return;
            }

            String json = objectMapper.writeValueAsString(Map.of(
                    "id", artwork.getId().toString(),
                    "title", value(artwork.getTitle()),
                    "description", value(artwork.getDescription()),
                    "category", value(artwork.getCategory()),
                    "style", value(artwork.getStyle()),
                    "medium", value(artwork.getMedium()),
                    "status", artwork.getStatus().name(),
                    "featured", artwork.isFeatured(),
                    "yearCreated", artwork.getYearCreated() == null ? 0 : artwork.getYearCreated(),
                    "createdAt", artwork.getCreatedAt().toString()
            ));

            client.put().uri("/" + INDEX + "/_doc/" + artwork.getId())
                    .header("Content-Type", "application/json")
                    .body(json)
                    .retrieve().toBodilessEntity();
        } catch (Exception ignored) {
            // Search is an enhancement; PostgreSQL remains the source of truth.
        }
    }

    public void delete(UUID id) {
        if (!enabled || id == null) return;
        try {
            client.delete().uri("/" + INDEX + "/_doc/" + id)
                    .retrieve().toBodilessEntity();
        } catch (Exception ignored) {
        }
    }

    private String buildQuery(String q, String category, Pageable pageable) throws Exception {
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> must = new ArrayList<>();
        List<Map<String, Object>> filters = new ArrayList<>();

        must.add(Map.of("term", Map.of("status", "PUBLISHED")));
        if (q != null && !q.isBlank()) {
            must.add(Map.of("multi_match", Map.of(
                    "query", q.trim(),
                    "fields", List.of("title^4", "category^3", "style^2", "medium^2", "description"),
                    "fuzziness", "AUTO"
            )));
        } else {
            must.add(Map.of("match_all", Map.of()));
        }
        if (category != null && !category.isBlank()) {
            filters.add(Map.of("term", Map.of("category", category.trim())));
        }

        bool.put("must", must);
        if (!filters.isEmpty()) bool.put("filter", filters);

        Map<String, Object> query = Map.of(
                "from", pageable.getOffset(),
                "size", pageable.getPageSize(),
                "query", Map.of("bool", bool),
                "sort", List.of(Map.of("_score", "desc"), Map.of("createdAt", "desc"))
        );
        return objectMapper.writeValueAsString(query);
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
