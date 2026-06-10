package com.decade.nexa.documents.application;

import com.decade.nexa.documents.application.ports.out.KnowledgeEngineGraph;
import com.decade.nexa.documents.application.ports.out.LogRepository;
import com.decade.nexa.documents.domain.IndexLog;
import com.decade.nexa.documents.domain.LogStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GraphManagement {
    final KnowledgeEngineGraph graph;
    final LogRepository logs;

    public void prepare(LocalDate date) {
        IndexLog log = new IndexLog(date);
        logs.save(log);
    }

    public void index(LocalDate date) {
        logs.findByIndexDate(date).ifPresent(log -> {
            graph.index(log.getRequestId());
            log.markAsRunning();
            logs.save(log);
        });
    }

    public void check(LocalDate date) {
        logs.findByIndexDate(date).ifPresent(log -> {
            if (graph.isFinished(log.getRequestId())) {
                log.markAsCompleted();
                logs.save(log);
            }
        });
    }

    public void deadline(LocalDate date) {
        logs.findByIndexDate(date).ifPresent(log -> {
            if (log.getStatus() != LogStatus.COMPLETED) {
                log.markAsFailed("Indexing took too long.");
                logs.save(log);
            }
        });
    }

}
