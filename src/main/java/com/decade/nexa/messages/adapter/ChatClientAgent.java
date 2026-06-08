package com.decade.nexa.messages.adapter;

import com.decade.nexa.documents.api.DocumentBotApi;
import com.decade.nexa.messages.application.ports.out.Agent;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatClientAgent implements Agent, InitializingBean {

    public static final String PERSONA = """
        You are a library keeper of our documentation system and helping the user to quickly find useful
        information about our stored documents. Please answer based on the provided context,
        If the provided context didn't explicitly say about it, just try your best answering the user,
        If the user has just started chatting (by measuring the time between current and previous message).
        Give him a greeting, sth like "Good morning, how can I help you today, Teri?".)
        """;

    Advisor longTermAdvisor;
    Advisor shortTermAdvisor;

    final DocumentBotApi botApi;
    final VectorStore vectorStore;
    final ChatMemoryRepository chatMemoryRepository;


    @Override
    public Flux<String> ask(String docId, UUID userId, String question) {
        return botApi.get(docId)
            .system(PERSONA)
            .user(question)
            .advisors(longTermAdvisor, shortTermAdvisor)
            .stream().chatResponse().map(r -> Optional.of(r.getResult())
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .orElse(""))
            .filter(StringUtils::hasLength);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        longTermAdvisor = VectorStoreChatMemoryAdvisor.builder(vectorStore).build();
        shortTermAdvisor = MessageChatMemoryAdvisor.builder(MessageWindowChatMemory
            .builder()
            .chatMemoryRepository(chatMemoryRepository)
            .maxMessages(15)
            .build()).build();
    }

}
