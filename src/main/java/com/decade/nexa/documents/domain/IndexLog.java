package com.decade.nexa.documents.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDate;
import java.util.UUID;

@RedisHash("knowledge-log")
@NoArgsConstructor
@Getter
public class IndexLog extends AbstractAggregateRoot<IndexLog> {
    @Id
    private LocalDate date;

    private UUID requestId;
    private LogStatus status;
    private String message;

    public IndexLog(LocalDate date) {
        this.date = date;
        this.requestId = UUID.randomUUID();
        this.status = LogStatus.CREATED;
    }

    public void markAsRunning() {
        this.status = LogStatus.RUNNING;
    }

    public void markAsFailed(String message) {
        this.status = LogStatus.FAILED;
        this.message = message;
    }

    public void markAsCompleted() {
        this.status = LogStatus.COMPLETED;
    }

    public void markAsIncompleted() {
        this.status = LogStatus.IN_COMPLETED;
    }

}
