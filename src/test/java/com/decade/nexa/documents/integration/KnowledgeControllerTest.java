package com.decade.nexa.documents.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
class KnowledgeControllerTest extends DocumentModuleIntegrationTest {

    final MockMvc mvc;

    @Test
    @WithMockUser
    void shouldAskKnowledgeEngine() throws Exception {
        mvc.perform(post("/knowledge/ask")
                .param("query", "what is life?"))
            .andExpect(status().isOk())
            .andExpect(content().string("hello world"));
    }
}
