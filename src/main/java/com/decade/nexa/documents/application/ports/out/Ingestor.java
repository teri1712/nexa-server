package com.decade.nexa.documents.application.ports.out;

import com.decade.nexa.documents.domain.DocType;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Ingestor {
    CompletableFuture<Void> ingest(DocType docType, List<Document> documents);
}
