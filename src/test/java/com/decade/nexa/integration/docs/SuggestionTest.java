package com.decade.nexa.integration.docs;

import com.decade.nexa.documents.adapters.AiSuggestionService;
import com.decade.nexa.integration.BaseTestClass;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.Evaluator;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;


class SuggestionTest extends BaseTestClass {

      @Autowired
      private Evaluator evaluator;

      @Autowired
      private AiSuggestionService ai;

      @Test
      void givenSqlAskingQuery_whenAISuggest_thenAISuggestionIsRelevant() {
            String query = "what are sql optimization techniques";
            ChatResponse response = ai.suggestImmediately(new Prompt(query));
            EvaluationRequest request = new EvaluationRequest(
                      query,
                      response.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS),
                      response.getResult().getOutput().getText()
            );
            assertThat(evaluator.evaluate(request).isPass()).isTrue();
      }
}
