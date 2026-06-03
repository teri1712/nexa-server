package com.decade.nexa.faq.adapters;

import com.decade.nexa.faq.application.ports.out.Synthesizer;
import com.decade.nexa.faq.domain.FAQ;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AiSynthesizer implements Synthesizer, InitializingBean {
    private final static String PERSONA = """
        You are the Nexa Knowledge Architect, a specialist in analyzing documentation search patterns and user inquiries.
        Your role is to distill collections of raw, often messy user queries into professional, representative FAQ questions.
        You are precise, user-centric, and excel at identifying the core intent behind varied phrasings.
        """;

    private final static String SYNTHESIS_PROMPT = """
        The following is a list of user queries related to our documentation system that have been semantically grouped.
        Analyze these queries and synthesize them into one single, high-quality FAQ question that best represents the group's intent.
        
        Requirements:
        1. Phrasing: Write from a first-person user perspective (e.g., 'How do I...', 'Where can I find...').
        2. Scope: The question should be comprehensive enough to cover the common themes in the cluster.
        3. Quality: Use proper grammar, professional tone, and clear terminology.
        4. Constraints: Return ONLY the text of the synthesized question. No introduction, no quotes, and no concluding remarks.
        
        Input Queries:
        {text}
        """;

    final ChatClient.Builder builder;
    ChatClient chatClient;

    @Override
    public List<String> synthesize(List<FAQ> clusters) {
        return clusters.stream().map(cluster -> {
            String synthesized = chatClient.prompt()
                .user(u -> u.text(SYNTHESIS_PROMPT)
                    .param("text", cluster.getQueries()))
                .call()
                .content();
            return synthesized;
        }).toList();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        chatClient = builder
            .defaultSystem(PERSONA)
            .build();
    }
}
