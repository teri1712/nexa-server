package com.decade.nexa.documents.adapters;

import com.decade.nexa.documents.application.LogManagement;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class IndexScheduler {
    final LogManagement management;

    @Scheduled(cron = "${nexa.graph.start}")
    @SchedulerLock(name = "graph-creating", lockAtLeastFor = "30s", lockAtMostFor = "1m")
    public void onPrepare() {
        management.prepare(LocalDate.now());
    }

    @Scheduled(cron = "${nexa.graph.index}")
    @SchedulerLock(name = "graph-indexing", lockAtLeastFor = "30s", lockAtMostFor = "1m")
    public void onIndex() {
        management.index(LocalDate.now());
    }

    @Scheduled(cron = "${nexa.graph.check-interval}")
    public void onCheck() {
        management.check(LocalDate.now());
    }

    @Scheduled(cron = "${nexa.graph.deadline}")
    public void onDeadline() {
        management.deadline(LocalDate.now());
    }
}
