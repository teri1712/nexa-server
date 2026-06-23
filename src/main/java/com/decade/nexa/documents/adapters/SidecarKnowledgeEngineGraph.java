package com.decade.nexa.documents.adapters;

import com.decade.nexa.documents.application.ports.out.Ingestor;
import com.decade.nexa.documents.application.ports.out.KnowledgeEngine;
import com.decade.nexa.documents.domain.DocType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SidecarKnowledgeEngineGraph implements Ingestor, KnowledgeEngine {

    final GraphSideCar sideCar;

    @Override
    public String ask(String query) {
        return sideCar.query(query);
    }

    private String makeFilename(String docId, String name) {
        return docId + "." + name + ".txt";
    }

    @Override
    public void ingest(String docId, String name, DocType docType, List<Document> chunks) {
        log.info("Updating knowledge graph with {} chunks.", chunks.size());
        String txt = String.join("\n", chunks.stream().map(Document::getText).toList());
        sideCar.upload(new ByteArrayResource(txt.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return makeFilename(docId, name);
            }
        });
        log.info("Knowledge graph updated.");
    }

    @Override
    public void egest(String docId, String name) {
        sideCar.delete(makeFilename(docId, name));
    }
}
