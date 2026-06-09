package com.decade.nexa.messages.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(name = "user_messages")
public class UserMessage extends Message {
    public UserMessage(String message, UUID userId, String docId) {
        super(message, userId, docId);
    }
}
