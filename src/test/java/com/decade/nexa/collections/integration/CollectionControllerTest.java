package com.decade.nexa.collections.integration;

import com.decade.nexa.collections.application.ports.out.CollectionRepository;
import com.decade.nexa.collections.domain.Collection;
import com.decade.nexa.common.ComponentTest;
import com.decade.nexa.common.jwt.WithJwtUser;
import com.decade.nexa.documents.api.DocInfo;
import com.decade.nexa.documents.api.DocumentApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
@ComponentTest(datasets = {CollectionDataset.class})
class CollectionControllerTest {

    final MockMvc mvc;
    final ObjectMapper objectMapper;
    final CollectionRepository collectionRepository;

    @MockitoSpyBean
    DocumentApi documentApi;

    @Test
    @WithJwtUser
    void givenUserHasNoCollections_whenCreatingAndListing_thenCollectionsAreReturnedAndItemsCanBeAddedAndAggregated() throws Exception {
        // 1. Initially collections list must be empty
        mvc.perform(get("/collections"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(0));

        // 2. Create a collection
        String response = mvc.perform(post("/collections")
                .param("name", "My Favorites"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.name").value("My Favorites"))
            .andReturn().getResponse().getContentAsString();

        // Extract ID
        Long collectionId = objectMapper.readTree(response).get("id").asLong();

        // 3. List collections again - must contain 1
        mvc.perform(get("/collections"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].id").value(collectionId))
            .andExpect(jsonPath("$[0].name").value("My Favorites"));

        // 4. Add items to collection
        mvc.perform(post("/collections/" + collectionId + "/items")
                .param("docId", "doc-123"))
            .andExpect(status().isCreated());

        // Stub DocumentApi
        doReturn(Map.of("doc-123", new DocInfo("doc-123", "Sample Title", "sample.pdf")))
            .when(documentApi).find(Set.of("doc-123"));

        // 5. List items in collection - must return aggregated doc info
        mvc.perform(get("/collections/" + collectionId + "/items"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].docId").value("doc-123"))
            .andExpect(jsonPath("$[0].title").value("Sample Title"))
            .andExpect(jsonPath("$[0].filename").value("sample.pdf"));
    }

    @Test
    @WithJwtUser
        // Defaults to Alice (11111111-1111-1111-1111-111111111111)
    void givenBobHasACollection_whenAliceTriesToAccess_thenForbiddenIsReturned() throws Exception {
        // Retrieve Bob's collection ID
        UUID bobId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Collection bobCollection = collectionRepository.findByUserId(bobId).stream()
            .findFirst()
            .orElseThrow();
        Long bobCollectionId = bobCollection.id();

        // Alice tries to retrieve Bob's collection items - must be forbidden
        mvc.perform(get("/collections/" + bobCollectionId))
            .andExpect(status().isForbidden());

        // Alice tries to add an item to Bob's collection - must be forbidden
        mvc.perform(post("/collections/" + bobCollectionId + "/items")
                .param("docId", "doc-123"))
            .andExpect(status().isForbidden());
    }
}

