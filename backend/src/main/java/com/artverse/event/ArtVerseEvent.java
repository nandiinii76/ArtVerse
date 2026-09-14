package com.artverse.event;

import java.time.Instant;
import java.util.Map;

public record ArtVerseEvent(
        String type,
        Instant occurredAt,
        Map<String, Object> data
) {
}
