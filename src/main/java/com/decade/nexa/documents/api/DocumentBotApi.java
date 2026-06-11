package com.decade.nexa.documents.api;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import reactor.core.publisher.Flux;

public interface DocumentBotApi {
    Flux<String> generate(String docId, String question, Advisor... advisors);
}
