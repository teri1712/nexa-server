package com.decade.nexa.messages.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AttributeOverride(name = "content", column = @Column(name = "content", nullable = false))
@Table(name = "user_messages")
public class UserMessage extends Message {
    public UserMessage(String message, UUID userId) {
        super(message, userId);
    }
}
