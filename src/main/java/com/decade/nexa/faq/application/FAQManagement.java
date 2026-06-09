package com.decade.nexa.faq.application;

import com.decade.nexa.documents.domain.events.UserSearched;
import com.decade.nexa.faq.application.ports.out.FAQRepository;
import com.decade.nexa.faq.application.ports.out.QueryRepository;
import com.decade.nexa.faq.application.ports.out.Synthesizer;
import com.decade.nexa.faq.domain.FAQ;
import com.decade.nexa.faq.domain.FaqClusteringFinished;
import com.decade.nexa.faq.domain.UserQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FAQManagement {

    final FAQRepository faqs;
    final QueryRepository queries;
    private final Synthesizer synthesizer;


    @ApplicationModuleListener
    public void on(FaqClusteringFinished event) {
        LocalDate date = event.date();
        List<FAQ> clusters = faqs.findByCreatedAt(date);

        if (clusters.isEmpty()) {
            log.debug("No clusters found for {} to synthesize.", date);
            return;
        }

        log.info("Detected completed clustering for {}. Starting LLM synthesis...", date);
        List<String> synthesized = synthesizer.synthesize(clusters);
        for (int i = 0; i < clusters.size(); i++) {
            FAQ faq = clusters.get(i);
            faq.setQuestion(synthesized.get(i));
        }
        faqs.saveAll(clusters);
    }

    @ApplicationModuleListener
    void on(UserSearched event) {
        queries.save(new UserQuery(null, event.query(), LocalDate.now()));
    }

}
