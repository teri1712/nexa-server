package com.decade.nexa.faq.integration;

import com.decade.nexa.common.ComponentTest;
import com.decade.nexa.faq.application.ports.out.FaqClusterer;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@RequiredArgsConstructor
@ComponentTest(datasets = {FaqDataset.class})
public class ClusterControllerTest {

    final MockMvc mvc;

    @MockitoBean
    FaqClusterer clusterer;

    @Test
    @WithMockUser(username = "teri", roles = "ADMIN")
    void givenLogDoesNotExist_whenFindByDate_thenReturn404AndListIsEmpty() throws Exception {
        LocalDate today = LocalDate.now();
        mvc.perform(get("/cluster-logs/{date}", today))
            .andExpect(status().isNotFound());

        mvc.perform(get("/cluster-logs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(username = "teri", roles = "ADMIN")
    void givenTriggerAlrCompleted_whenListAndFind_mustReturnNewlyTodayLog() throws Exception {
        LocalDate today = LocalDate.now();

        // Mock clusterer to finish immediately when checked
        when(clusterer.isFinish(anyLong())).thenReturn(true);

        // Trigger clustering
        MvcResult mvcResult = mvc.perform(post("/cluster-logs/trigger"))
            .andExpect(request().asyncStarted())
            .andReturn();

        mvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk());

        mvc.perform(get("/cluster-logs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.size()").value(1))
            .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));

        // Verify find by date
        mvc.perform(get("/cluster-logs/{date}", today))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value(today.toString()))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
