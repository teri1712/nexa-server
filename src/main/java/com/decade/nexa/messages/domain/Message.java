package com.decade.nexa.messages.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@NoArgsConstructor
public abstract class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long sequenceId;

    @Column(name = "content", nullable = true)
    @Setter(AccessLevel.PROTECTED)
    private String content;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "created_at")
    private Instant createdAt;

    protected Message(String content, UUID userId) {
        this.content = content;
        this.userId = userId;
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }
}
