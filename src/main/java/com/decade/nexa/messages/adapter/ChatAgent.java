package com.decade.nexa.messages.adapter;

import com.decade.nexa.messages.application.ports.out.Agent;
import com.decade.nexa.users.api.UserInfoTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalTime;
import java.util.UUID;

@Component
public class ChatAgent implements Agent {

    public static final String PERSONA = """
                        You are a library keeper of our documentation system and helping the user to quickly find useful
                        information about our stored documents. Please answer based on the provided context,
                        If the provided context didn't explicitly say about it, just try your best answering the user,
                        If the user has just started chatting (by measuring the time between current and previous message).
                        Give him a greeting, sth like "Good morning, how can I help you today, Teri?".)
        """;


    private final ChatClient chatClient;
    private final UserInfoTools userInfoTools;

    public ChatAgent(ChatClient.Builder builder, VectorStore vectorStore, ChatMemoryRepository chatMemoryRepository, UserInfoTools userInfoTools, LocalTools localTools) {
        this.userInfoTools = userInfoTools;
        chatClient = builder
            .defaultTools(userInfoTools, localTools)
            .defaultAdvisors(VectorStoreChatMemoryAdvisor.builder(vectorStore).build()) // long term
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(15)
                .build()).build()) // recency
            .defaultSystem(PERSONA)
            .build();
    }


    @Override
    public Flux<String> ask(UUID userId, String question) {
        return this.chatClient.prompt()
            .user(question)
            .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userId))
            .toolContext(userInfoTools.prepareContext(userId))
            .stream()
            .content();
    }


    @Component
    public static class LocalTools {

        @Tool(description = "Get local time", name = "get_local_time")
        public LocalTime getTime() {
            return LocalTime.now();
        }
    }
}
