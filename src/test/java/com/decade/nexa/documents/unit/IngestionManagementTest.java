package com.decade.nexa.documents.unit;


import com.decade.nexa.documents.application.IngestionManagement;
import com.decade.nexa.documents.application.ports.out.Ingestor;
import com.decade.nexa.documents.application.ports.out.ReaderResolver;
import com.decade.nexa.documents.domain.DocType;
import com.decade.nexa.documents.domain.events.DocCreated;
import com.decade.nexa.files.apis.FileApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.core.io.Resource;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IngestionManagementTest {

    @Mock
    FileApi fileApi;

    @Mock
    Ingestor ingestor;

    @Mock
    DocumentTransformer transformer;

    @Mock
    ReaderResolver readerResolver;

    IngestionManagement ingestionManagement;

    @BeforeEach
    void setUp() {
        ingestionManagement = new IngestionManagement(fileApi, List.of(readerResolver), List.of(ingestor), List.of(transformer));
    }

    @Test
    void shouldCallETLPipelineAndPopulateToIngestors() {
        Instant today = Instant.now();

        Resource resource = mock(Resource.class);
        Document document1 = mock(Document.class);
        Document document2 = mock(Document.class);

        DocumentReader reader = mock(DocumentReader.class);

        when(fileApi.getResource(eq("123"))).thenReturn(resource);

        when(readerResolver.resolve(eq(DocType.PDF), any())).thenReturn(reader);
        when(reader.read()).thenReturn(List.of(document1));

        when(transformer.apply(eq(List.of(document1)))).thenReturn(List.of(document2));

        var event = new DocCreated("123", DocType.PDF, today);
        ingestionManagement.on(event);

        verify(fileApi).getResource(eq("123"));
        verify(readerResolver).resolve(eq(DocType.PDF), any());
        verify(transformer).apply(eq(List.of(document1)));
        verify(ingestor).ingest(eq("123"), eq(DocType.PDF), eq(List.of(document2)));
    }
}
