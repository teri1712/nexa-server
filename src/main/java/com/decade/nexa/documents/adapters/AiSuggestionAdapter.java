package com.decade.nexa.documents.adapters;

import com.decade.nexa.documents.application.ports.out.SuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AiSuggestionAdapter implements SuggestService {
      private final AiSuggestionService ai;

      @Override
      public Flux<String> suggest(String query) {
            return ai.suggest(new Prompt(query)).map(r -> Optional.ofNullable(r.getResult())
                                .map(Generation::getOutput)
                                .map(AbstractMessage::getText)
                                .orElse(""))
                      .filter(StringUtils::hasLength);
      }
}
