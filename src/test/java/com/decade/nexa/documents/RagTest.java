package com.decade.nexa.documents;

import com.decade.nexa.common.BaseTestClass;
import com.decade.nexa.documents.domain.DocCreated;
import com.decade.nexa.documents.domain.DocType;
import com.decade.nexa.documents.infras.Rag;
import com.decade.nexa.files.apis.FileApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

@Slf4j
@RequiredArgsConstructor
class RagTest extends BaseTestClass {

    @MockitoBean
    FileApi fileApi;
    
    final Rag rag;
    final ElasticsearchOperations es;
    final VectorStore vectorStore;
    final DocumentCleanUp cleanUp;

    @BeforeEach
    void clean() {
        cleanUp.clean();
    }

    @Test
    void testVectorsSaved(Scenario scenario) {
        Mockito.when(fileApi.getResource(anyString())).thenReturn(
            new InputStreamResource(getClass().getResourceAsStream("/samples/sql.pdf"))
        );

        scenario.publish(new DocCreated("vcl", DocType.PDF, Instant.now()))
            .andWaitForStateChange(() -> {
                es.indexOps(IndexCoordinates.of("nexa-documents")).refresh();
                return es.count(Query.findAll(), IndexCoordinates.of("nexa-documents"));
            })
            .andVerify(count -> assertThat(count).isGreaterThan(5));

        List<Document> docs = vectorStore.similaritySearch("What are SQL optimization techniques");
        log.debug("Rag doc hits: {}", docs);
        assertThat(docs).isNotEmpty();

        ChatResponse response = rag.suggest(new Prompt("What are SQL optimization techniques")).call().chatResponse();
        log.debug("Rag response: {}", response.getResult().getOutput().getText());
    }
}
