package com.decade.nexa.documents.domain;

import java.time.Instant;

public record DocCreated(String id, DocType contentType, Instant createdAt) {
}
