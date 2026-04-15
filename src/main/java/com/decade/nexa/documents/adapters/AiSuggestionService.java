package com.decade.nexa.documents.adapters;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

public interface AiSuggestionService {
      Flux<ChatResponse> suggest(Prompt prompt);

      ChatResponse suggestImmediately(Prompt prompt);
}
