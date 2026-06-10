package com.decade.nexa.faq.application.ports.out;

import com.decade.nexa.faq.domain.ClusterLog;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ClusterLogRepository extends CrudRepository<ClusterLog, Long> {
    Optional<ClusterLog> findByClusterDate(LocalDate date);
}


