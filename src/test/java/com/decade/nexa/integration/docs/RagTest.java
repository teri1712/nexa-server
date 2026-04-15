package com.decade.nexa.integration.docs;

import com.decade.nexa.documents.domain.DocType;
import com.decade.nexa.documents.infras.Rag;
import com.decade.nexa.files.apis.FileApi;
import com.decade.nexa.integration.BaseTestClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

@Slf4j
class RagTest extends BaseTestClass {

      @MockitoBean
      private FileApi fileApi;

      @Autowired
      private Rag rag;


      @Autowired
      private ElasticsearchOperations es;

      @Autowired
      private VectorStore vectorStore;

      @Test
      void testVectorsSaved() {
            Mockito.when(fileApi.getResource(anyString())).thenReturn(
                      new InputStreamResource(getClass().getResourceAsStream("/samples/sql.pdf"))
            );

            rag.ingest(DocType.PDF, fileApi.getResource("sql.pdf"));

            long count = es.count(Query.findAll(), IndexCoordinates.of("nexa-documents"));
            log.debug("count nexa-documents: {}", count);
            assertThat(count).isGreaterThan(5);

            List<Document> docs = vectorStore.similaritySearch("What are SQL optimization techniques");
            log.debug("Rag doc hits: {}", docs);
            assertThat(docs).isNotEmpty();


            ChatResponse response = rag.suggestImmediately(new Prompt("What are SQL optimization techniques"));
            log.debug("Rag response: {}", response.getResult().getOutput().getText());
      }
}
