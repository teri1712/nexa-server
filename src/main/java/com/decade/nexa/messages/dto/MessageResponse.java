package com.decade.nexa.messages.dto;

import java.io.Serializable;
import java.time.Instant;

public record MessageResponse(Long sequenceId, String message, Instant createdAt) implements Serializable {
}