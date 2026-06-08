package com.decade.nexa.documents.adapters;

import com.decade.nexa.documents.api.DocumentBotApi;
import com.decade.nexa.documents.application.ports.out.Ingestor;
import com.decade.nexa.documents.domain.DocType;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentBotRag implements Ingestor, InitializingBean, DocumentBotApi {

    final VectorStore vectorStore;
    final ChatClient.Builder builder;
    ChatClient chatClient;

    @Override
    @Observed(name = "ingest.document")
    public void ingest(String docId, DocType docType, List<Document> chunks) {
        chunks.forEach(chunk -> {
            chunk.getMetadata().put("docId", docId);
        });
        vectorStore.add(chunks);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        this.chatClient = builder
            .defaultAdvisors(RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .similarityThreshold(0.7)
                    .topK(5)
                    .build()
                )
                .build())
            .build();
    }

    @Override
    public ChatClient.ChatClientRequestSpec get(String docId) {
        return chatClient.prompt()
            .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, "docId == '%s'".formatted(docId)));
    }

}
