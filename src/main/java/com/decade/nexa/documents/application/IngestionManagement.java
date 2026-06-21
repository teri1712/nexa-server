package com.decade.nexa.documents.application;

import com.decade.nexa.documents.application.ports.out.Ingestor;
import com.decade.nexa.documents.application.ports.out.ReaderResolver;
import com.decade.nexa.documents.domain.events.DocCreated;
import com.decade.nexa.documents.domain.events.DocDeleted;
import com.decade.nexa.files.apis.FileApi;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionManagement {

    final FileApi fileApi;
    final List<ReaderResolver> readers;
    final List<Ingestor> ingestors;
    final List<DocumentTransformer> transformers;

    public void on(DocDeleted docDeleted) {
        ingestors.forEach(ingestor -> {
            ingestor.egest(docDeleted.id(), docDeleted.filename());
        });
    }

    public void on(DocCreated docCreated) {
        Resource file = fileApi.getResource(docCreated.fileKey());
        List<Document> documents = null;
        for (ReaderResolver reader : readers) {
            val docReader = reader.resolve(docCreated.contentType(), file);
            if (Objects.nonNull(docReader)) {
                documents = docReader.read();
                break;
            }
        }
        if (documents == null)
            return;
        log.info("Documents read: {}", documents.size());

        for (DocumentTransformer transformer : transformers)
            documents = transformer.apply(documents);

        log.info("Documents transformed: {}", documents.size());

        for (Ingestor ingestor : ingestors)
            ingestor.ingest(docCreated.id(), docCreated.filename(), docCreated.contentType(), documents);
        log.info("Documents propagated to {} ingestors", ingestors.size());
    }

    @Async
    @Observed(name = "ingestion", lowCardinalityKeyValues = {"thread", "platform"})
    @EventListener(id = "ingest-management-platform",
        condition = "@environment.getProperty('spring.threads.virtual.enabled', 'false') == 'false'")
    void platform(DocCreated docCreated) {
        on(docCreated);
    }

    @Async
    @Observed(name = "ingestion", lowCardinalityKeyValues = {"thread", "virtual"})
    @EventListener(id = "ingest-management-virtual",
        condition = "@environment.getProperty('spring.threads.virtual.enabled', 'false') == 'true'")
    void virtual(DocCreated docCreated) {
        on(docCreated);
    }
}