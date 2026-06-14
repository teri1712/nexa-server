package com.decade.nexa.documents.integration;


import com.decade.nexa.documents.adapters.IndexScheduler;
import com.decade.nexa.documents.application.IngestionManagement;
import com.decade.nexa.documents.domain.DocType;
import com.decade.nexa.documents.domain.events.DocCreated;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RequiredArgsConstructor
public class KnowledgeEngineGraphTest extends DocumentModuleIntegrationTest {

    final IndexScheduler scheduler;
    final IngestionManagement ingestion;
    final MockMvc mockMvc;

    @Test
    @WithMockUser(username = "teri", roles = "ADMIN")
    void shouldAnswerSomeMeaningFullContext_afterIndexing() throws Exception {
        doReturn(new ClassPathResource("/samples/sql.pdf")).when(fileApi).getResource("123");
        ingestion.on(new DocCreated("123", "123", DocType.PDF, Instant.now()));

        assertDoesNotThrow(() -> {
            scheduler.onPrepare();
            scheduler.onIndex();
            scheduler.onCheck();
            scheduler.onDeadline();
        });

        mockMvc.perform(post("/knowledge/ask")
                .content(MediaType.APPLICATION_JSON_VALUE)
                .param("query", "what do you know about SQL")
            )
            .andExpect(content().string("hello world"));
    }


}
