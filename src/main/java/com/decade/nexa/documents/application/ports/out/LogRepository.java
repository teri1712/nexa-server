package com.decade.nexa.documents.application.ports.out;

import com.decade.nexa.documents.domain.IndexLog;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface LogRepository extends CrudRepository<IndexLog, Long> {
    Optional<IndexLog> findByIndexDate(LocalDate date);
}

