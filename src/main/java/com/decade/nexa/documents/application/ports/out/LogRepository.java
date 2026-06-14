package com.decade.nexa.documents.application.ports.out;

import com.decade.nexa.documents.domain.IndexLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface LogRepository extends CrudRepository<IndexLog, Long>, PagingAndSortingRepository<IndexLog, Long> {
    Optional<IndexLog> findByIndexDate(LocalDate date);
    Page<IndexLog> findAll(Pageable pageable);
}

