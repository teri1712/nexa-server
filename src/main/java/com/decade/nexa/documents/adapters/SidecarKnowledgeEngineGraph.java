package com.decade.nexa.documents.adapters;

import com.decade.nexa.documents.application.ports.out.Ingestor;
import com.decade.nexa.documents.application.ports.out.KnowledgeEngineGraph;
import com.decade.nexa.documents.domain.DocType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SidecarKnowledgeEngineGraph implements Ingestor, KnowledgeEngineGraph {

    final GraphSideCar sideCar;


    @Override
    public void index(Long requestId) {
        sideCar.index(requestId);
    }

    @Override
    public boolean isFinished(Long requestId) {
        Map<String, String> body = sideCar.progress(requestId);
        String status = body.get("status");
        return switch (status) {
            case "completed" -> {
                log.debug("Python indexing is completed for {}.", requestId);
                yield true;
            }
            case "error", "failed" -> throw new RuntimeException("Indexing job failed on Python side for %s: %s".formatted(requestId, body.get("message")));
            default -> false;
        };
    }

    @Override
    public String ask(String query) {
        return sideCar.query(query);
    }

    @Override
    public void ingest(String docId, String name, DocType docType, List<Document> chunks) {
        log.info("Updating knowledge graph with {} chunks.", chunks.size());
        String txt = String.join("\n", chunks.stream().map(Document::getText).toList());
        sideCar.upload(new ByteArrayResource(txt.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return docId + "." + name + ".txt";
            }
        });
        log.info("Knowledge graph updated.");
    }
}
