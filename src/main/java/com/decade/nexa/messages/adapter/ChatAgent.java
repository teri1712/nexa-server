package com.decade.nexa.messages.adapter;

import com.decade.nexa.messages.application.ports.out.Agent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Component
public class ChatAgent implements Agent {

    private final ChatClient chatClient;

    public ChatAgent(ChatClient.Builder builder, VectorStore vectorStore, ChatMemoryRepository chatMemoryRepository) {
        chatClient = builder
            .defaultAdvisors(VectorStoreChatMemoryAdvisor.builder(vectorStore).build()) // long term
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(15)
                .build()).build()) // recency
            .defaultSystem("""
                You are a library keeper of our document system
                You know everything and helping the user to quickly find useful
                information about documents. Please answer based on the provided context,
                If the provided context didn't explicitly say about it, just say you dont know
                or if you can answer it yourself, you can answer it based on your knowledge
                """)
            .build();
    }


    @Override
    public Flux<String> ask(UUID userId, String question) {
        return this.chatClient.prompt()
            .user(question)
            .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userId))
            .stream()
            .content();
    }
}
