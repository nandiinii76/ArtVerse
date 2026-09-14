package com.artverse.social;

import com.artverse.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social/comments")
public class CommentController {
    private final CommentRepository repository;
    public CommentController(CommentRepository repository){this.repository=repository;}

    public record CreateRequest(@NotBlank @Size(max=2000) String body) {}
    public record Response(UUID id, UUID artworkId, UUID userId, String body, java.time.Instant createdAt) {
        static Response from(Comment c){return new Response(c.getId(),c.getArtworkId(),c.getUserId(),c.getBody(),c.getCreatedAt());}
    }

    @GetMapping("/artwork/{artworkId}")
    public Page<Response> list(@PathVariable UUID artworkId, @RequestParam(defaultValue="0") int page){
        return repository.findByArtworkIdOrderByCreatedAtDesc(artworkId, PageRequest.of(Math.max(0,page),30)).map(Response::from);
    }

    @PostMapping("/artwork/{artworkId}")
    public Response create(@PathVariable UUID artworkId, @Valid @RequestBody CreateRequest request, @AuthenticationPrincipal User user){
        Comment c=new Comment(); c.setArtworkId(artworkId); c.setUserId(user.getId()); c.setBody(request.body()); return Response.from(repository.save(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal User user){
        Comment c=repository.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Comment not found"));
        if(!c.getUserId().equals(user.getId())) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Only the author can delete this comment");
        repository.delete(c); return ResponseEntity.noContent().build();
    }
}
