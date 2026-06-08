package com.decade.nexa.documents.integration;

import com.decade.nexa.documents.application.DocService;
import com.decade.nexa.documents.application.ports.out.DocumentRepository;
import com.decade.nexa.documents.domain.DocType;
import com.decade.nexa.documents.domain.Documentation;
import com.decade.nexa.documents.dto.CreateDocumentRequest;
import com.decade.nexa.files.apis.FileIntegrityException;
import com.decade.nexa.files.apis.FileMetadata;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@RequiredArgsConstructor
class DocServiceTest extends DocumentModuleIntegrationTest {

    final DocumentRepository docs;
    final DocService service;

    @Test
    void givenDocumentIsAdded_whenFetchingTheDocument_thenReturnFullDetail() throws FileIntegrityException {
        Mockito.doReturn(new FileMetadata("test", "pdf"))
            .when(fileApi).getFile("test", "test");
        service.add(new CreateDocumentRequest(
            "test.pdf",
            "test",
            "test",
            "title",
            "description",
            DocType.PDF));
        Documentation doc = assertDoesNotThrow(() -> docs.findById("test").orElseThrow());
        assertThat(doc.getContentType()).isEqualTo(DocType.PDF);
        assertThat(doc.getTitle()).isEqualTo("title");
        assertThat(doc.getDescription()).isEqualTo("description");
        assertThat(doc.getFilename()).isEqualTo("test.pdf");
    }

}
