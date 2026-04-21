package com.decade.nexa.messages.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.util.UUID;

@DiscriminatorValue("user")
@Entity
@NoArgsConstructor
public class UserMessage extends Message {
    public UserMessage(String message, UUID userId) {
        super(message, userId);
    }
}
