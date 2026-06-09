package com.decade.nexa.documents.domain.events;

import com.decade.nexa.documents.domain.DocType;

import java.time.Instant;

public record DocCreated(String id, DocType contentType, Instant createdAt) {
}
