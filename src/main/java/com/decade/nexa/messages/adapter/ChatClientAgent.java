package com.decade.nexa.messages.adapter;

import com.decade.nexa.documents.api.DocumentBotApi;
import com.decade.nexa.messages.application.ports.out.Agent;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatClientAgent implements Agent, InitializingBean {

    Advisor longTermAdvisor;
    Advisor shortTermAdvisor;

    final DocumentBotApi botApi;
    final VectorStore vectorStore;
    final ChatMemoryRepository chatMemoryRepository;


    @Override
    public Flux<String> generate(String docId, UUID userId, String question) {
        return botApi.generate(docId, question, longTermAdvisor, shortTermAdvisor);
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
