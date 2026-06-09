package com.decade.nexa.documents.domain;

import com.decade.nexa.documents.domain.events.IndexCompleted;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@Table(name = "knowledge_log")
@NoArgsConstructor
@Getter
public class IndexLog extends AbstractAggregateRoot<IndexLog> {

    @Id
    @Column("id")
    private UUID requestId;

    private LocalDate indexDate;

    private LogStatus status;
    private String message;

    public IndexLog(LocalDate indexDate) {
        this.indexDate = indexDate;
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
        registerEvent(new IndexCompleted(indexDate));
    }

}
