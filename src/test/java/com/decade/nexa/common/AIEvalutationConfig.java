package com.decade.nexa.common;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AIEvalutationConfig {
      @Bean
      RelevancyEvaluator relevancyEvaluator(ChatModel chatModel) {
            return new RelevancyEvaluator(ChatClient.builder(chatModel));
      }
}
