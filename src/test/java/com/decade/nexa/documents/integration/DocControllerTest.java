package com.decade.nexa.documents.integration;

import com.decade.nexa.documents.domain.DocType;
import com.decade.nexa.documents.dto.CreateDocumentRequest;
import com.decade.nexa.files.apis.FileMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
class DocControllerTest extends DocumentModuleIntegrationTest {

    final MockMvc mvc;

    @Test
    @WithMockUser(username = "teri", roles = "ADMIN")
    void givenDocumentMetadataAlrPosted_whenSearchingTheDoc_thenReturnSubmittedDocInfos() throws Exception {

        doReturn(new FileMetadata("teri.pdf", "pdf"))
            .when(fileApi).getFile("test", "test");

        mvc.perform(post("/docs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new CreateDocumentRequest(
                    "teri.pdf",
                    "test",
                    "test",
                    "title",
                    "description",
                    DocType.PDF)))
            )
            .andExpect(status().isAccepted());

        // search
        mvc.perform(get("/docs")
                .queryParam("query", "teri.pdf")
                .queryParam("type", "PDF")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.docs.size()").value(1))
            .andExpect(jsonPath("$.docs[0].title").value("title"))
            .andExpect(jsonPath("$.docs[0].fileType").value("PDF"))
            .andExpect(jsonPath("$.docs[0].filename").value("teri.pdf"))
            .andReturn()
        ;

        mvc.perform(get("/docs")
                .queryParam("query", "vcl")
                .queryParam("type", "PDF")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalPages").value(0));


    }

    @Test
    @WithMockUser(username = "teri", roles = "ADMIN")
    void givenFileNotExist_whenCallingDelete_mustReturn404() throws Exception {
        mvc.perform(delete("/docs/non-existent-doc-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "teri", roles = "ADMIN")
    void givenFileExists_whenCallingDeleteWithCorrectDocId_thenFileMustBeDeleted() throws Exception {
        // 1. Mock file API
        doReturn(new FileMetadata("seed-file.pdf", "pdf"))
            .when(fileApi).getFile("seed-key", "seed-etag");

        // 2. Seed file via POST /docs API
        String responseContent = mvc.perform(post("/docs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new CreateDocumentRequest(
                    "seed-file.pdf",
                    "seed-etag",
                    "seed-key",
                    "seed-title",
                    "seed-description",
                    DocType.PDF)))
            )
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // 3. Extract the generated docId
        String docId = new ObjectMapper().readTree(responseContent).get("id").asText();

        // 4. Call delete with the correct docId
        mvc.perform(delete("/docs/" + docId))
            .andExpect(status().isNoContent());

        // 5. Validate the file is deleted via GET /docs/{docId} (should return 404)
        mvc.perform(get("/docs/" + docId))
            .andExpect(status().isNotFound());
    }
}
