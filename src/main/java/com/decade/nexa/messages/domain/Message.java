package com.decade.nexa.messages.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "message_type")
@Getter
@Table(name = "messages")
public abstract class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long sequenceId;

    private String message;

    @Column(name = "created_at")
    private Instant createdAt;

}
