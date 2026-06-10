package com.decade.nexa.faq.unit;

import com.decade.nexa.faq.application.FAQManagement;
import com.decade.nexa.faq.application.ports.out.FAQRepository;
import com.decade.nexa.faq.application.ports.out.QueryRepository;
import com.decade.nexa.faq.application.ports.out.Synthesizer;
import com.decade.nexa.faq.domain.FAQ;
import com.decade.nexa.faq.domain.FaqClusteringFinished;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FaqPipelineTest {


    @Mock
    FAQRepository faqs;

    @Mock
    Synthesizer synthesizer;

    @Mock
    QueryRepository queries;

    @InjectMocks
    FAQManagement faqManagement;

    @Captor
    ArgumentCaptor<List<FAQ>> faqsCaptor;

    @Test
    void shouldLoadTodayFaqAndSaveNewOnesWithQuestionsNotNull() {
        LocalDate today = LocalDate.now();
        List<FAQ> todayFaqList = List.of(new FAQ(
                99L,
                null,
                List.of(
                    "What is Kubernetes?",
                    "Explain Kubernetes",
                    "K8s overview"
                ),
                LocalDate.of(2026, 6, 2)
            ),
            new FAQ(
                100L,
                null,
                List.of(
                    "What is a Pod?",
                    "Kubernetes Pod explained",
                    "Purpose of a Pod"
                ),
                LocalDate.of(2026, 6, 2)
            ),
            new FAQ(
                101L,
                null,
                List.of(
                    "What is a Service?",
                    "Kubernetes Service explained",
                    "How do Pods communicate?"
                ),
                LocalDate.of(2026, 6, 2)
            ));
        List<String> synthesizedQuestions = List.of("What is Kubernetes?", "What is a Pod in Kubernetes?", "What is a Service in Kubernetes?");

        when(faqs.findByCreatedAt(eq(today))).thenReturn(todayFaqList);

        when(synthesizer.synthesize(eq(todayFaqList))).thenReturn(synthesizedQuestions);

        faqManagement.on(new FaqClusteringFinished(today));

        verify(faqs).findByCreatedAt(today);
        verify(synthesizer).synthesize(todayFaqList);
        verify(faqs).saveAll(faqsCaptor.capture());

        List<FAQ> savedFaqs = faqsCaptor.getValue();

        assertThat(savedFaqs).extracting(FAQ::getQuestion)
            .containsExactlyElementsOf(synthesizedQuestions);

        assertThat(savedFaqs).extracting(FAQ::getClusterId)
            .containsExactly(99L, 100L, 101L);
    }

}
