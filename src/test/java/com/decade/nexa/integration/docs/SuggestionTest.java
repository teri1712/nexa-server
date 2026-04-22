package com.decade.nexa.integration.docs;

import com.decade.nexa.documents.adapters.AiSuggestionService;
import com.decade.nexa.integration.BaseTestClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.Evaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@TestPropertySource(properties = "spring.ai.ollama.chat.model=phi3")
class SuggestionTest extends BaseTestClass {

    @Autowired
    Evaluator evaluator;

    @Autowired
    AiSuggestionService ai;

    @Test
    void givenSqlAskingQuery_whenAISuggest_thenAISuggestionIsRelevant() {
        String query = "what are sql optimization techniques";
        ChatResponse response = ai.suggestImmediately(new Prompt(query));
        log.info("AI response: {}", response.getResult().getOutput().getText());
        EvaluationRequest request = new EvaluationRequest(
            query,
            response.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS),
            response.getResult().getOutput().getText()
        );
        assertThat(evaluator.evaluate(request).isPass()).isTrue();
    }
}
