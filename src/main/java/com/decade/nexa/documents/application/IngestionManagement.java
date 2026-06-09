package com.decade.nexa.documents.application;

import com.decade.nexa.documents.application.ports.out.Ingestor;
import com.decade.nexa.documents.application.ports.out.ReaderResolver;
import com.decade.nexa.documents.domain.events.DocCreated;
import com.decade.nexa.files.apis.FileApi;
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
    final DocumentTransformer transformer;

    @Async
    @EventListener(id = "ingest-management")
    public void on(DocCreated docCreated) {
        Resource file = fileApi.getResource(docCreated.id());
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

        val proccessed = transformer.apply(documents);
        log.info("Documents processed: {}", proccessed.size());
        log.info("Documents propagating to {} ingestors", ingestors.size());
        ingestors.forEach(ingestor ->
            ingestor.ingest(docCreated.id(), docCreated.contentType(), proccessed)
        );
    }
}
