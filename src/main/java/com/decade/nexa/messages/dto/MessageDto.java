package com.decade.nexa.messages.dto;

import java.io.Serializable;
import java.time.Instant;

public record MessageDto(Long sequenceNumber, String content, Instant createdAt, boolean mine) implements Serializable {
}