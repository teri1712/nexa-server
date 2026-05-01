package com.decade.nexa.documents.infras;

import org.springframework.ai.document.Document;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PictureBuilder {
    CompletableFuture<Void> build(List<Document> docs);
}
