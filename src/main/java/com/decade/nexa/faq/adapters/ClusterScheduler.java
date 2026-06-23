package com.decade.nexa.faq.adapters;

import com.decade.nexa.faq.application.FAQManagement;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClusterScheduler {

    final FAQManagement faqManagement;

    @Scheduled(cron = "${nexa.faq.deadline}")
    @SchedulerLock(name = "faq-clustering", lockAtLeastFor = "5m", lockAtMostFor = "10m")
    public void onDeadline() {
        faqManagement.cluster();
    }
}
