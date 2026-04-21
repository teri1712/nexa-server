package com.decade.nexa.messages.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.util.UUID;

@DiscriminatorValue("agent")
@Entity
@NoArgsConstructor
public class AgentMessage extends Message {
    public AgentMessage(String message, UUID userId) {
        super(message, userId);
    }
}
