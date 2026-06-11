package com.decade.nexa.faq.application.ports.out;

import com.decade.nexa.faq.domain.ClusterLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ClusterLogRepository extends CrudRepository<ClusterLog, Long>, PagingAndSortingRepository<ClusterLog, Long> {
    Optional<ClusterLog> findByClusterDate(LocalDate date);
    Page<ClusterLog> findAll(Pageable pageable);
}


