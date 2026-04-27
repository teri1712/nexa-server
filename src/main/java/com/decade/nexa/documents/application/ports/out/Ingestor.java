package com.decade.nexa.documents.application.ports.out;

import com.decade.nexa.documents.domain.DocType;
import org.springframework.ai.document.Document;

import java.util.List;

public interface Ingestor {
    void ingest(DocType docType, List<Document> documents);
}
