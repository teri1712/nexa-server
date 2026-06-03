package com.decade.nexa.documents;

import com.decade.nexa.common.BaseTestClass;
import com.decade.nexa.documents.application.ports.out.NexaObjectRepository;
import com.decade.nexa.documents.domain.DocType;
import com.decade.nexa.documents.domain.events.DocCreated;
import com.decade.nexa.documents.infras.Picture;
import com.decade.nexa.documents.infras.PictureRetriever;
import com.decade.nexa.files.apis.FileApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

@Slf4j
@RequiredArgsConstructor
@ActiveProfiles({"test", "gg", "graph-rag"})
@TestPropertySource(properties = "spring.ai.model.embedding=google-genai")
class GraphRagIngestionTest extends BaseTestClass {

    @MockitoBean
    FileApi fileApi;

    @Qualifier("local-graph")
    @Autowired
    PictureRetriever graphPicture;
    final NexaObjectRepository nexaObjects;
    final DocumentCleanUp cleanUp;
    final Neo4JClean neo4jClean;

    @BeforeEach
    void clean() {
        cleanUp.clean();
        neo4jClean.clean();
    }

    @Test
    void shouldIngestSuccessfullyAndSaveSth(Scenario scenario) {
        Mockito.when(fileApi.getResource(anyString())).thenReturn(
            new InputStreamResource(getClass().getResourceAsStream("/samples/endava.pdf"))
        );

        scenario.publish(new DocCreated("vcl", DocType.PDF, Instant.now()))
            .andWaitForStateChange(nexaObjects::count)
            .andVerify(count -> assertThat(count).isGreaterThan(5));

        List<Picture> pictures = graphPicture.retrieve(Query.builder().text("What are sql tecniques").build());

        assertThat(pictures)
            .hasSizeGreaterThan(0);
        pictures.forEach(picture -> log.debug("picture: {}", picture));
    }
}
