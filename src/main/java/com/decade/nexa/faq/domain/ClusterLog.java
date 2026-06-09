package com.decade.nexa.faq.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table(name = "cluster_log")
@NoArgsConstructor
@Getter
public class ClusterLog extends AbstractAggregateRoot<ClusterLog> {

    @Id
    private Long id;

    @Column("cluster_date")
    private LocalDate clusterDate;

    private LogStatus status;
    private String message;

    public ClusterLog(LocalDate clusterDate) {
        this.clusterDate = clusterDate;
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
        registerEvent(new FaqClusteringFinished(clusterDate));
    }

    public Long getRequestId() {
        return id;
    }

}
