package com.decade.nexa.documents.application;

import com.decade.nexa.documents.application.ports.out.KnowledgeEngineGraph;
import com.decade.nexa.documents.application.ports.out.LogRepository;
import com.decade.nexa.documents.domain.IndexLog;
import com.decade.nexa.documents.domain.LogStatus;
import com.decade.nexa.documents.domain.events.IndexCompleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphManagement {
    final KnowledgeEngineGraph graph;
    final ApplicationEventPublisher publisher;
    final LogRepository logs;

    @Value("${nexa.graph.poll-interval}")
    private Duration pollInterval;

    @Scheduled(cron = "${nexa.graph.cron}")
    @SchedulerLock(name = "indexing", lockAtLeastFor = "5m", lockAtMostFor = "1h")
    public void indexing() throws Exception {
        LocalDate today = LocalDate.now();
        try {
            IndexLog log = logs.findById(today).orElseGet(() -> new IndexLog(today));
            if (log.getStatus() == LogStatus.CREATED) {
                // there's a very edge case, but just in case
                graph.index(log.getRequestId());
                log.markAsRunning();
            }
            logs.save(log);

            while (!graph.isFinished(log.getRequestId()))
                Thread.sleep(pollInterval);

            log.markAsCompleted();
            logs.save(log);

            publisher.publishEvent(new IndexCompleted(today));
        } catch (Exception e) {
            log.error("Critical error indexing the graph", e);
            IndexLog errorLog = new IndexLog(today);
            errorLog.markAsFailed(e.getMessage());

            logs.save(errorLog);
            throw e;
        }
    }


}
