package com.decade.nexa.files.domain.events;

public record UploadCompleted(String id, String url, String filename) {
}
