package com.decade.nexa.faq.integration;

import com.decade.nexa.common.ComponentTest;
import com.decade.nexa.common.OpenAiDataset;
import com.decade.nexa.documents.domain.events.UserSearched;
import com.decade.nexa.faq.adapters.ClusterScheduler;
import com.decade.nexa.faq.application.ports.out.FAQRepository;
import com.decade.nexa.faq.domain.FAQ;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
@ComponentTest(datasets = {FaqDataset.class, OpenAiDataset.class})
public class FaqControllerTest {

    final MockMvc mvc;
    final ClusterScheduler scheduler;
    final FAQRepository faqs;
    final ApplicationEventPublisher publisher;

    @Test
    @WithMockUser(username = "teri", roles = "ADMIN")
    void givenItsAlr3Am_whenUserQueryFaq_thenSomeFaqMustBeReturned() throws Exception {
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
            publisher.publishEvent(new UserSearched(queries.get(i)));
        }

        // Simulate the execution of the Python clustering service, which directly
        // inserts clustered queries into the faq table in the database
        faqs.saveAll(List.of(
            new FAQ(1L, null, List.of("How do I reset my password?", "I forgot my password"), LocalDate.now()),
            new FAQ(2L, null, List.of("How do I cancel my subscription?", "Stop my monthly plan"), LocalDate.now()),
            new FAQ(3L, null, List.of("Where is my order?", "Track my shipment"), LocalDate.now())
        ));

        // Trigger the scheduler to detect completed clusters and perform LLM synthesis
        scheduler.onDeadline();

        // When & Then
        mvc.perform(MockMvcRequestBuilders.get("/faqs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(3));
    }
}
