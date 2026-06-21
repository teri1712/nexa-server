package com.decade.nexa.documents.application;

import com.decade.nexa.documents.dto.CreateDocumentRequest;
import com.decade.nexa.documents.dto.DocumentResponse;
import com.decade.nexa.files.apis.FileIntegrityException;

public interface DocService {

    DocumentResponse add(CreateDocumentRequest request) throws FileIntegrityException;

    void delete(String id);

    DocumentResponse find(String id);
}
