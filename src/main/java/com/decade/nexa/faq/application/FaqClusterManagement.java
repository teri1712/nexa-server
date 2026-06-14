package com.decade.nexa.faq.application;

import com.decade.nexa.faq.application.ports.out.ClusterLogRepository;
import com.decade.nexa.faq.application.ports.out.FaqClusterer;
import com.decade.nexa.faq.domain.ClusterLog;
import com.decade.nexa.faq.domain.LogClusterStartupPolicy;
import com.decade.nexa.faq.domain.LogStatus;
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
public class FaqClusterManagement {
    final FaqClusterer clusterer;
    final ClusterLogRepository logs;
    final LogClusterStartupPolicy startupPolicy;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClusterLog prepare(LocalDate date) {
        log.info("Preparing clustering for {}", date);
        ClusterLog log = new ClusterLog(date);
        startupPolicy.apply(log);
        logs.save(log);
        return log;
    }

    public void cluster(LocalDate date) {
        log.info("Clustering for {}", date);
        logs.findByClusterDate(date).ifPresent(log -> {
            clusterer.cluster(log.getRequestId(), date);
            log.markAsRunning();
            logs.save(log);
        });
    }

    public Page<ClusterLog> list(Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "clusterDate");
        return logs.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
    }

    public ClusterLog find(LocalDate date) {
        return logs.findByClusterDate(date).orElseThrow();
    }

    public boolean check(LocalDate date) {
        Optional<ClusterLog> logOptional = logs.findByClusterDate(date);
        if (logOptional.isEmpty()) {
            return false;
        } else {
            log.info("Checking clustering for {}", date);
            var log = logOptional.get();
            if (log.getStatus() == LogStatus.RUNNING
                && clusterer.isFinish(log.getRequestId())) {
                log.markAsCompleted();
                logs.save(log);
                return true;
            }
            return false;
        }
    }

    public void deadline(LocalDate date) {
        log.info("Deadline for {}", date);
        logs.findByClusterDate(date).ifPresent(log -> {
            if (log.getStatus() != LogStatus.COMPLETED) {
                log.markAsFailed("Clustering took too long.");
                logs.save(log);
            }
        });
    }

}
