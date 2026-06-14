package com.decade.nexa.documents.application;

import com.decade.nexa.documents.application.ports.out.KnowledgeEngineGraph;
import com.decade.nexa.documents.application.ports.out.LogRepository;
import com.decade.nexa.documents.domain.IndexLog;
import com.decade.nexa.documents.domain.LogIndexStartupPolicy;
import com.decade.nexa.documents.domain.LogStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LogManagement {
    final KnowledgeEngineGraph graph;
    final LogRepository logs;
    final LogIndexStartupPolicy startupPolicy;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void prepare(LocalDate date) {
        log.info("Preparing indexing for {}", date);
        IndexLog log = new IndexLog(date);
        startupPolicy.apply(log);
        logs.save(log);
    }

    public void index(LocalDate date) {
        log.info("Indexing for {}", date);
        logs.findByIndexDate(date).ifPresent(log -> {
            graph.index(log.getRequestId());
            log.markAsRunning();
            logs.save(log);
        });
    }

    public Page<IndexLog> list(Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "indexDate");
        return logs.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
    }

    public IndexLog find(LocalDate date) {
        return logs.findByIndexDate(date).orElseThrow();
    }

    public boolean check(LocalDate date) {
        log.info("Checking indexing for {}", date);
        Optional<IndexLog> logOptional = logs.findByIndexDate(date);
        if (logOptional.isEmpty()) {
            return false;
        } else {
            var log = logOptional.get();
            if (log.getStatus() == LogStatus.RUNNING
                && graph.isFinished(log.getRequestId())) {
                log.markAsCompleted();
                logs.save(log);
                return true;
            }
            return false;
        }
    }

    public void deadline(LocalDate date) {
        log.info("Deadline for {}", date);
        logs.findByIndexDate(date).ifPresent(log -> {
            if (log.getStatus() != LogStatus.COMPLETED) {
                log.markAsFailed("Indexing took too long.");
                logs.save(log);
            }
        });
    }

}
