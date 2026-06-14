package com.decade.nexa.documents.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
public class IndexControllerTest extends DocumentModuleIntegrationTest {

    final MockMvc mvc;

    @Test
    @WithMockUser(username = "teri", roles = "ADMIN")
    void givenLogDoesNotExist_whenFindByDate_thenReturn404AndListIsEmpty() throws Exception {
        LocalDate today = LocalDate.now();
        mvc.perform(get("/index-logs/{date}", today))
            .andExpect(status().isNotFound());

        mvc.perform(get("/index-logs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(username = "teri", roles = "ADMIN")
    void givenTriggerAlrCompleted_whenListAndFind_mustReturnNewlyTodayLog() throws Exception {
        LocalDate today = LocalDate.now();

        // Trigger indexing
        mvc.perform(post("/index-logs/trigger"))
            .andExpect(status().isAccepted())
            .andReturn();

        mvc.perform(get("/index-logs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.size()").value(1))
            .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));

        // Verify find by date
        mvc.perform(get("/index-logs/{date}", today))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value(today.toString()))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
