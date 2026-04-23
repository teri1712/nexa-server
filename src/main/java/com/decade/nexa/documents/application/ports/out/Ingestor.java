package com.decade.nexa.documents.application.ports.out;

import com.decade.nexa.documents.domain.DocType;
import org.springframework.core.io.Resource;

public interface Ingestor {
      void ingest(DocType docType, Resource file);
}
