package com.decade.nexa.documents.adapters;

import com.decade.nexa.documents.application.ports.out.ReaderResolver;
import com.decade.nexa.documents.domain.DocType;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PdfReaderResolver implements ReaderResolver {


    @Override
    public DocumentReader resolve(DocType docType, Resource resource) {
        if (docType == DocType.PDF) {
            return new PagePdfDocumentReader(resource);
        }
        return null;
    }
}
