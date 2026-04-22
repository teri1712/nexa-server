package com.decade.nexa.documents.application;

import com.decade.nexa.documents.dto.CreateDocumentRequest;
import com.decade.nexa.documents.dto.DocumentResponse;
import com.decade.nexa.files.apis.FileIntegrityException;

public interface DocService {

    void add(CreateDocumentRequest request) throws FileIntegrityException;

    DocumentResponse find(String id);
}
