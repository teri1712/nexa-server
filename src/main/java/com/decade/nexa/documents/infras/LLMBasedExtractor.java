package com.decade.nexa.documents.infras;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LLMBasedExtractor implements EntityExtractor, InitializingBean {
    private final ChatClient.Builder builder;
    private ChatClient chatClient;

    @Override
    public Extraction extract(String context) {
        BeanOutputConverter<Extraction> converter =
            new BeanOutputConverter<>(Extraction.class);

        String persona = """
            You are an expert data extraction pipeline. Read the following text and extract all
            meaningful entities (people, organizations, concepts, tools) and the relationships between them.
            
            Text to analyze:
            {text}
            
            You must return ONLY valid JSON matching this schema.
            Do not include explanations, markdown, or extra text.
            I dont accept null or empty values.
            
            {format}
            """;

        Extraction extractedData = chatClient.prompt()
            .user(u -> u.text(persona)
                .param("text", context)
                .param("format", converter.getFormat()))
            .call()
            .entity(converter);

        if (extractedData == null || extractedData.entities().isEmpty()) {
            log.warn("No entities extracted from the text.");
        }

        log.info("Extracted {} entities and {} relationships.",
            extractedData.entities().size(), extractedData.relationships().size());

        return extractedData;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        chatClient = builder.build();
    }
}
