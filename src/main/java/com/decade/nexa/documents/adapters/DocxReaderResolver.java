package com.decade.nexa.documents.adapters;

import com.decade.nexa.documents.application.ports.out.ReaderResolver;
import com.decade.nexa.documents.domain.DocType;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class DocxReaderResolver implements ReaderResolver {


    @Override
    public DocumentReader resolve(DocType docType, Resource resource) {
        if (docType == DocType.DOCX) {
            return new TikaDocumentReader(resource);
        }
        return null;
    }
}
