package com.decade.nexa.files.application.ports.out;

import com.decade.nexa.files.domain.UploadRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<UploadRecord, String> {
}
