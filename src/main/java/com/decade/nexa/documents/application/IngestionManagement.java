package com.decade.nexa.documents.application;

import com.decade.nexa.documents.application.ports.out.Ingestor;
import com.decade.nexa.documents.domain.DocCreated;
import com.decade.nexa.files.apis.FileApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestionManagement {

      private final FileApi fileApi;
      private final List<Ingestor> ingestors;


      @EventListener
      @Async
      void on(DocCreated docCreated) {
            Resource file = fileApi.getResource(docCreated.id());
            ingestors.forEach(i -> i.ingest(docCreated.contentType(), file));
      }
}
