package com.decade.nexa.documents.integration;

import com.decade.nexa.documents.application.DocService;
import com.decade.nexa.documents.application.ports.in.SearchService;
import com.decade.nexa.documents.application.ports.out.DocumentRepository;
import com.decade.nexa.documents.domain.DocType;
import com.decade.nexa.documents.dto.CreateDocumentRequest;
import com.decade.nexa.documents.dto.DocFilter;
import com.decade.nexa.files.apis.FileIntegrityException;
import com.decade.nexa.files.apis.FileMetadata;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@RequiredArgsConstructor
class SearchServiceTest extends DocumentModuleIntegrationTest {

    final SearchService searchService;
    final DocumentRepository docs;
    final DocService docService;

    @Test
    void givenDocAlrExist_whenSearching_thenReturnTheDoc() throws FileIntegrityException {
        Mockito.doReturn(new FileMetadata("test.pdf", "PDF"))
            .when(fileApi).getFile("test", "test");

        CreateDocumentRequest request = new CreateDocumentRequest(
            "test.pdf",
            "test",
            "test",
            "title",
            "description",
            DocType.PDF);
        assertDoesNotThrow(() -> {
            docService.add(request);
        });

        assertThat(docs.findAll()).hasSize(1);

        assertThat(searchService.search(
            new DocFilter("test.pdf",
                Instant.now().minus(Duration.ofDays(2)),
                Instant.now(),
                DocType.PDF,
                null)).docs()).hasSize(1);
    }
}
