package com.artverse.search;

import com.artverse.artwork.ArtworkRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertNull;

class ArtworkSearchServiceTest {
    @Test
    void disabledOpenSearchReturnsNullSoCallerCanUseDatabaseFallback() {
        ArtworkRepository repository = Mockito.mock(ArtworkRepository.class);
        ArtworkSearchService service = new ArtworkSearchService(
                repository,
                new ObjectMapper(),
                "http://localhost:9200",
                false);

        Page<?> result = service.search("monsoon", null, PageRequest.of(0, 12));

        assertNull(result);
        Mockito.verifyNoInteractions(repository);
    }
}
