package com.decade.nexa.messages.integration;

import com.decade.nexa.common.ComponentTest;
import com.decade.nexa.common.OpenAiDataset;
import com.decade.nexa.common.jwt.WithJwtUser;
import com.decade.nexa.messages.dto.MessagePlacedDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@ComponentTest(datasets = {MessageDataset.class, OpenAiDataset.class})
@RequiredArgsConstructor
public class FulfillmentControllerTest {

    final MockMvc mvc;
    final ObjectMapper objectMapper;

    @Test
    @WithJwtUser
    void givenPlaceholderMessageExists_whenUserFillePlaceholder_thenAnswerContentMustBeCreated() throws Exception {
        String docId = "123";
        // given
        Long placeHolderId = objectMapper.readValue(mvc.perform(post("/messages")
                    .param("docId", docId)
                    .param("message", "Hello 1"))
                .andReturn()
                .getResponse().getContentAsString(), MessagePlacedDto.class)
            .placeHolderMessage().sequenceNumber();

        // when
        MvcResult mvcResult = mvc.perform(post("/fill")
                .contentType("application/json")
                .param("placeholderSequence", placeHolderId.toString())
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk());

        // then

        mvc.perform(get("/messages")
                .queryParam("docId", docId)
                .queryParam("anchorSeq", Long.toString(10000L))
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].sequenceNumber").value(placeHolderId))
            .andExpect(jsonPath("$[0].content").isNotEmpty())
        ;
    }
}
