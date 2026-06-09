package com.decade.nexa.faq.integration;

import com.decade.nexa.common.BaseTestClass;
import com.decade.nexa.documents.domain.events.UserSearched;
import com.decade.nexa.faq.application.FAQManagement;
import com.decade.nexa.faq.application.ports.out.QueryRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.Scenario;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
@ActiveProfiles({"test", "ollama"})
public class FaqControllerTest extends BaseTestClass {
    final MockMvc mvc;
    final FAQManagement faqManagement;
    final QueryRepository queries;

    @Test
    @WithMockUser(username = "teri", roles = "ADMIN")
    void givenItsAlr3Am_whenUserQueryFaq_thenSomeFaqMustBeReturned(Scenario scenario) throws Exception {
        // Given
        List<String> queries = List.of(
            // FAQ 1: Password Reset
            "How do I reset my password?",
            "I forgot my password",
            "Can't log in because I don't remember my password",
            "Where can I change my account password?",
            "Password recovery process",

            // FAQ 2: Subscription & Billing
            "How do I cancel my subscription?",
            "Stop my monthly plan",
            "How can I update my payment method?",
            "When will I be charged?",
            "Billing history not showing",

            // FAQ 3: Order Tracking
            "Where is my order?",
            "Track my shipment",
            "How do I check delivery status?",
            "My package hasn't arrived",
            "When will my order be delivered?"
        );
        for (int i = 0; i < queries.size(); i++) {
            final int j = i;
            scenario.publish(new UserSearched(queries.get(i)))
                .andWaitForStateChange(() -> {
                    var count = this.queries.count();
                    return count > j ? count : null;
                })
                .andVerify(count -> {
                    assertThat(count).isEqualTo(j + 1);
                });
        }
        assertThat(this.queries.count()).isEqualTo(15);
        queries.stream().map(UserSearched::new).forEach(new Consumer<UserSearched>() {
            @Override
            public void accept(UserSearched event) {
                scenario.publish(event)
                    .andWaitForStateChange(FaqControllerTest.this.queries::count)
                    .andVerify(count -> {
                    });
            }
        });

        faqManagement.runFaqPipeline(); // 1AM hit
        // When
        // Then

        mvc.perform(MockMvcRequestBuilders.get("/faqs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(3));
    }


}
