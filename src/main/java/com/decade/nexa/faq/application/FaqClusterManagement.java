package com.decade.nexa.faq.application;

import com.decade.nexa.faq.application.ports.out.ClusterLogRepository;
import com.decade.nexa.faq.application.ports.out.FaqClusterer;
import com.decade.nexa.faq.domain.ClusterLog;
import com.decade.nexa.faq.domain.LogStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    public void prepare(LocalDate date) {
        ClusterLog log = new ClusterLog(date);
        logs.save(log);
    }

    public void cluster(LocalDate date) {
        logs.findByClusterDate(date).ifPresent(log -> {
            clusterer.cluster(log.getRequestId(), date);
            log.markAsRunning();
            logs.save(log);
        });
    }

    public boolean check(LocalDate date) {
        Optional<ClusterLog> logOptional = logs.findByClusterDate(date);
        if (logOptional.isEmpty()) {
            return false;
        } else {
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
        logs.findByClusterDate(date).ifPresent(log -> {
            if (log.getStatus() != LogStatus.COMPLETED) {
                log.markAsFailed("Clustering took too long.");
                logs.save(log);
            }
        });
    }

}
