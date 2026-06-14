package com.decade.nexa.documents.domain;

import com.decade.nexa.documents.application.ports.out.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogIndexStartupPolicy {
    final LogRepository logs;

    public void apply(IndexLog indexLog) {
        logs.findByIndexDate(indexLog.getIndexDate()).ifPresent(log -> {
            if (log.getStatus() == LogStatus.COMPLETED) {
                throw new IllegalStateException("Clustering already finished.");
            } else if (log.getStatus() != LogStatus.FAILED) {
                throw new IllegalStateException("Clustering already started.");
            }
            logs.delete(log);
        });
    }
}
