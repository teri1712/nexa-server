package com.decade.nexa.documents.domain.events;

public record DocDeleted(String id, String fileKey, String filename) {
}
