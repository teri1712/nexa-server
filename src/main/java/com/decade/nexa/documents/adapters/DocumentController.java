package com.decade.nexa.documents.adapters;

import com.decade.nexa.documents.application.DocService;
import com.decade.nexa.documents.application.ports.in.SearchService;
import com.decade.nexa.documents.application.ports.out.SuggestService;
import com.decade.nexa.documents.domain.DocType;
import com.decade.nexa.documents.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Instant;

@RestController
@RequestMapping("/docs")
@RequiredArgsConstructor
@Validated
public class DocumentController {

      private final DocService docService;
      private final SearchService searchService;
      private final SuggestService suggestService;

      @PostMapping
      @ResponseStatus(HttpStatus.ACCEPTED)
      void upload(@Valid @RequestBody CreateDocumentRequest request) {
            docService.add(request);
      }

      @GetMapping("/{id}")
      DocumentResponse find(@PathVariable String id) {
            return docService.find(id);
      }

      @GetMapping
      DocPage search(
                @NotBlank @RequestParam String query,
                @RequestParam(required = false) Instant start,
                @RequestParam(required = false) Instant end,
                @RequestParam DocType type,
                @RequestParam(required = false) String lastDocId,
                @RequestParam(required = false) Float lastDocScore) {
            if (start == null) {
                  start = Instant.parse("2026-01-01T00:00:00Z");
            }
            if (end == null) {
                  end = Instant.now();
            }
            LastDoc lastDoc = null;
            if (lastDocId != null) {
                  if (lastDocScore == null) {
                        lastDocScore = 0.0f;
                  }
                  lastDoc = new LastDoc(lastDocId, lastDocScore);
            }
            return searchService.search(new DocFilter(query, start, end, type, lastDoc));
      }

      @PostMapping(path = "suggest", produces = MediaType.TEXT_PLAIN_VALUE)
      public Flux<String> suggest(@RequestParam String query) {
            return suggestService.suggest(query);
      }
}
