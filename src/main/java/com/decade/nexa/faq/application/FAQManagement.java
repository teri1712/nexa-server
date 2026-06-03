package com.decade.nexa.faq.application;

import com.decade.nexa.documents.domain.events.UserSearched;
import com.decade.nexa.faq.application.ports.out.FAQRepository;
import com.decade.nexa.faq.application.ports.out.FaqClusterer;
import com.decade.nexa.faq.application.ports.out.QueryRepository;
import com.decade.nexa.faq.application.ports.out.Synthesizer;
import com.decade.nexa.faq.domain.FAQ;
import com.decade.nexa.faq.domain.FaqClusteringFinished;
import com.decade.nexa.faq.domain.UserQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FAQManagement {

    final FAQRepository faqs;
    final FaqClusterer clusterer;
    final QueryRepository queries;
    final ApplicationEventPublisher publisher;
    private final Synthesizer synthesizer;


    @Value("${nexa.faq.poll-interval}")
    private Duration pollInterval;

    @Scheduled(cron = "${nexa.faq.cron}")
    @SchedulerLock(name = "FAQ_Trigger_Job", lockAtMostFor = "1h", lockAtLeastFor = "5m")
    public void runFaqPipeline() {
        LocalDate today = LocalDate.now();
        log.info("Triggering FAQ Clustering job for date: {}...", today);
        try {
            clusterer.cluster(today);
            while (!clusterer.isFinish(today))
                Thread.sleep(pollInterval);

            publisher.publishEvent(new FaqClusteringFinished(today));

            log.info("Clustering job completed");
        } catch (Exception e) {
            log.error("Critical error triggering FAQ Clustering job", e);
        }
    }


    @EventListener
    @Transactional
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
