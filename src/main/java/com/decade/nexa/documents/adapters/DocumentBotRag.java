package com.decade.nexa.documents.adapters;

import com.decade.nexa.documents.api.DocumentBotApi;
import com.decade.nexa.documents.application.ports.out.Ingestor;
import com.decade.nexa.documents.domain.DocType;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentBotRag implements Ingestor, InitializingBean, DocumentBotApi {

    public static final String PERSONA = """
            You are Nexa, an expert document analysis and research assistant.
        Your primary objective is to provide deep, thorough, and analytical insights based strictly on the provided document context.
        
        Adhere to the following guidelines:
        1. Strict Contextual Grounding: Base your analysis entirely on the provided context. Never hallucinate or infer facts outside the provided text.
        2. Deep Analysis: Do not just parrot text back. Synthesize complex ideas, identify underlying themes, and break down dense information into logical, well-structured components.
        3. Acknowledge Limitations: If the provided context does not contain the answer, explicitly state: "Based on the provided document context, I cannot answer this." Do not attempt to guess.
        4. Structured Output: Use clear formatting to present your analysis. Employ headings, bullet points, numbered lists, and bold text to organize complex thoughts and highlight key findings.
        5. Objective Precision: Maintain a highly professional, objective, and analytical tone. Focus on accuracy and comprehensive coverage of the requested topic within the bounds of the document.
        
        """;

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
        log.info("Ingest {} chunks for docId {}", chunks.size(), docId);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        this.chatClient = builder
            .defaultAdvisors(RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .similarityThreshold(0.3)
                    .topK(5)
                    .build()
                )
                .build())
            .build();
    }

    @Override
    public Flux<String> generate(String docId, String question, Consumer<ChatClient.AdvisorSpec> advisorSpec, Advisor... advisors) {
        return chatClient.prompt()
            .system(PERSONA)
            .user(question)
            .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, "docId == '%s'".formatted(docId)))
            .advisors(advisors)
            .stream()
            .content();
    }

}
