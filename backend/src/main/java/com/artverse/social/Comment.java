package com.artverse.social;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="artwork_comments")
@Getter @Setter @NoArgsConstructor
public class Comment {
    @Id @GeneratedValue private UUID id;
    @Column(name="artwork_id", nullable=false) private UUID artworkId;
    @Column(name="user_id", nullable=false) private UUID userId;
    @Column(nullable=false, length=2000) private String body;
    @Column(nullable=false, updatable=false) private Instant createdAt;
    @PrePersist void create(){ createdAt=Instant.now(); }
}
