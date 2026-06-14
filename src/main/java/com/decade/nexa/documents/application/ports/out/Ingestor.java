package com.decade.nexa.documents.application.ports.out;

import com.decade.nexa.documents.domain.DocType;
import org.springframework.ai.document.Document;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface Ingestor {
    @Async
    void ingest(String docId, String name, DocType docType, List<Document> chunks);
}
