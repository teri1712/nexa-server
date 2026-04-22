package com.decade.nexa.integration.docs;

import com.decade.nexa.documents.application.ports.out.DocumentRepository;
import com.decade.nexa.documents.domain.DocType;
import com.decade.nexa.documents.dto.CreateDocumentRequest;
import com.decade.nexa.files.apis.FileApi;
import com.decade.nexa.files.apis.FileMetadata;
import com.decade.nexa.integration.BaseTestClass;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FileApi fileApi;

    @Autowired
    private DocumentRepository documents;

    @Test
    @WithMockUser(username = "teri", roles = "ADMIN")
    void givenPostDocumentMetadataAlrPosted_whenSearchingTheDoc_thenReturnSubmittedDocInfos() throws Exception {
        when(fileApi.getFile("test", "test"))
            .thenReturn(new FileMetadata("teri.pdf", "pdf"));
        mvc.perform(post("/docs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new CreateDocumentRequest(
                    "test.pdf",
                    "test",
                    "test",
                    "title",
                    "description",
                    DocType.PDF)))
            )
            .andExpect(status().isAccepted());

        mvc.perform(get("/docs")
                .queryParam("query", "teri.pdf")
                .queryParam("type", "PDF")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.docs.size()").value(1))
            .andExpect(jsonPath("$.docs[0].title").value("title"))
            .andExpect(jsonPath("$.docs[0].fileType").value("PDF"))
            .andExpect(jsonPath("$.docs[0].filename").value("test.pdf"))
        ;
    }
}
