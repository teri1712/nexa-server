package com.decade.nexa.documents.application.ports.out;

import com.decade.nexa.documents.domain.DocType;
import org.springframework.ai.document.DocumentReader;
import org.springframework.core.io.Resource;

public interface ReaderResolver {
    DocumentReader resolve(DocType docType, Resource resource);
}
