package com.decade.nexa.faq.integration;

import com.decade.nexa.common.ComponentTest;
import com.decade.nexa.documents.domain.events.UserSearched;
import com.decade.nexa.faq.adapters.ClusterScheduler;
import com.decade.nexa.faq.application.ports.out.QueryRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
@ComponentTest(datasets = {FaqDataset.class})
public class FaqControllerTest {

    final MockMvc mvc;
    final ClusterScheduler scheduler;
    final QueryRepository queries;
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
        scheduler.onPrepare();
        scheduler.onCluster();

        await().atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofSeconds(2))
            .untilAsserted(() -> {
                assertThat(scheduler.onCheck()).isTrue();
            });

        scheduler.onDeadline();

        // When
        // Then

        mvc.perform(MockMvcRequestBuilders.get("/faqs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(3));
    }


}
