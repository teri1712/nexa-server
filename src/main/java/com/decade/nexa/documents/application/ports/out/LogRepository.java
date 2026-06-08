package com.decade.nexa.documents.application.ports.out;

import com.decade.nexa.documents.domain.IndexLog;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;

public interface LogRepository extends CrudRepository<IndexLog, LocalDate> {
}
