package com.decade.nexa.documents.adapters;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(2)
public class QuestionContextualTransformer implements DocumentTransformer {

    public static final String PROMPT_TEMPLATE = """
        Analyze the following text and extract exactly 3 professional questions that this text provides definitive answers for.
        The questions should capture the core intent and key information of the content.
        Return ONLY the questions, each on a new line, without numbering, bullet points, or any introductory remarks.
        
        TEXT:
        {text}
        """;

    final ChatClient.Builder builder;
    final TaskExecutor taskScheduler;

    @Override
    @Observed(name = "HyDE.augmenting")
    public List<Document> apply(List<Document> documents) {
        ChatClient chatClient = builder.build();
        log.info("HyDE Augmenting {} documents.", documents.size());
        List<Document> transformed = documents.stream()
            .map(doc -> CompletableFuture.supplyAsync(() -> augmentWithQuestions(chatClient, doc), taskScheduler))
            .toList()
            .stream()
            .map(CompletableFuture::join)
            .toList();
        log.info("HyDE Augmented {} documents.", transformed.size());
        return transformed;
    }

    private Document augmentWithQuestions(ChatClient chatClient, Document document) {
        String questions = chatClient.prompt()
            .user(u -> u.text(PROMPT_TEMPLATE)
                .param("text", document.getText()))
            .call()
            .content();

        String enrichedContent = """
            %s
            
            Hypothetical Questions:
            %s
            """.formatted(document.getText(), questions);

        return new Document(document.getId(), enrichedContent, document.getMetadata());
    }
}
