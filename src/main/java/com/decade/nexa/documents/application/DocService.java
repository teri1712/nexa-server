package com.decade.nexa.documents.application;

import com.decade.nexa.documents.application.ports.out.DocumentRepository;
import com.decade.nexa.documents.domain.Documentation;
import com.decade.nexa.documents.dto.CreateDocumentRequest;
import com.decade.nexa.documents.dto.DocumentResponse;
import com.decade.nexa.files.apis.FileApi;
import com.decade.nexa.files.apis.FileMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocService {

      private final FileApi fileApi;
      private final DocumentRepository docs;

      public void add(CreateDocumentRequest request) {
            FileMetadata file = fileApi.getFile(request.fileKey(), request.eTag());
            Documentation documentation = new Documentation(request.fileKey(), request.filename(), request.title(), request.description(), request.type());
            docs.save(documentation);
      }

      public DocumentResponse find(String id) {
            return docs.findById(id).map(documentation ->
                      DocumentResponse.builder()
                                .id(documentation.getId())
                                .title(documentation.getTitle())
                                .filename(documentation.getFilename())
                                .description(documentation.getDescription())
                                .fileType(documentation.getContentType())
                                .createdAt(documentation.getCreatedAt())
                                .build()).orElseThrow();
      }
}
