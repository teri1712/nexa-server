package com.decade.nexa.faq.adapters;

import com.decade.nexa.faq.application.FaqClusterManagement;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ClusterScheduler {
    final FaqClusterManagement management;

    @Scheduled(cron = "${nexa.faq.start}")
    @SchedulerLock(name = "faq-preparing", lockAtLeastFor = "30s", lockAtMostFor = "1m")
    public void onPrepare() {
        management.prepare(LocalDate.now());
    }

    @Scheduled(cron = "${nexa.faq.cluster}")
    @SchedulerLock(name = "faq-clustering", lockAtLeastFor = "30s", lockAtMostFor = "1m")
    public void onCluster() {
        management.cluster(LocalDate.now());
    }

    @Scheduled(cron = "${nexa.faq.check-interval}")
    public boolean onCheck() {
        return management.check(LocalDate.now());
    }

    @Scheduled(cron = "${nexa.faq.deadline}")
    public void onDeadline() {
        management.deadline(LocalDate.now());
    }
}
