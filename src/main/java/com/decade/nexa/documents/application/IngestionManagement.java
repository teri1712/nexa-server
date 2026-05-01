package com.decade.nexa.documents.application;

import com.decade.nexa.documents.application.ports.out.Ingestor;
import com.decade.nexa.documents.domain.DocCreated;
import com.decade.nexa.documents.domain.DocType;
import com.decade.nexa.files.apis.FileApi;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class IngestionManagement {

    private final FileApi fileApi;
    private final List<Ingestor> ingestors;

    private TokenTextSplitter splitter() {
        return TokenTextSplitter.builder()
            .withChunkSize(800)
            .withMinChunkSizeChars(200)
            .withMinChunkLengthToEmbed(5)
            .withMaxNumChunks(5000)
            .withKeepSeparator(true)
            .build();
    }

    private List<Document> read(DocType type, Resource resource) {
        if (Objects.requireNonNull(type) == DocType.PDF) {
            return new PagePdfDocumentReader(resource).read();
        }
        return new TikaDocumentReader(resource).read();
    }

    @Async
    @TransactionalEventListener(id = "ingest-management")
    CompletableFuture<Void> on(DocCreated docCreated) {
        Resource file = fileApi.getResource(docCreated.id());
        List<Document> documents = read(docCreated.contentType(), file);
        val proccessed = splitter()
            .apply(documents);
        return CompletableFuture.allOf(ingestors.stream()
            .map(ingestor ->
                ingestor.ingest(docCreated.contentType(), proccessed))
            .toArray(CompletableFuture[]::new));
    }
}
