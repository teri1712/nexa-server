package com.decade.nexa.faq.domain;

import com.decade.nexa.faq.application.ports.out.ClusterLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogClusterStartupPolicy {
    final ClusterLogRepository logs;

    public void apply(ClusterLog clusterLog) {
        logs.findByClusterDate(clusterLog.getClusterDate()).ifPresent(log -> {
            if (log.getStatus() == LogStatus.COMPLETED) {
                throw new IllegalStateException("Clustering already finished.");
            } else if (log.getStatus() != LogStatus.FAILED) {
                throw new IllegalStateException("Clustering already started.");
            }
            logs.delete(log);
        });
    }
}
