package com.decade.nexa.messages.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "message_type")
@Getter
@Table(name = "messages")
@NoArgsConstructor
public abstract class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long sequenceId;

    private String message;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "created_at")
    private Instant createdAt;


    protected Message(String message, UUID userId) {
        this.message = message;
        this.userId = userId;
        this.createdAt = Instant.now();
    }
}
