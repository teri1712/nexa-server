package com.decade.nexa.messages.integration;

import com.decade.nexa.common.ComponentTest;
import com.decade.nexa.common.jwt.WithJwtUser;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentTest(datasets = MessageDataset.class)
@RequiredArgsConstructor
public class MessageControllerTest {
    final MockMvc mvc;

    @Test
    @WithJwtUser
    void givenUserAlrAsk2Times_whenQueryMesasgeList_shouldReturn4Messages() throws Exception {
        String docId = "123";
        mvc.perform(post("/messages")
                .contentType("application/json")
                .param("docId", docId)
                .param("message", "Hello 1"))
            .andExpect(status().isOk());

        mvc.perform(post("/messages")
                .contentType("application/json")
                .param("docId", docId)
                .param("message", "Hello 2"))
            .andExpect(status().isOk());

        mvc.perform(get("/messages")
                .queryParam("docId", docId)
                .queryParam("anchorSeq", Long.toString(10000L))
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(4))
            .andExpect(jsonPath("$[3].content").value("Hello 1"))
            .andExpect(jsonPath("$[1].content").value("Hello 2"))
        ;
    }

}
