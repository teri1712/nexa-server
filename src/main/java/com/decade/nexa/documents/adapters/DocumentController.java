package com.decade.nexa.documents.adapters;

import com.decade.nexa.documents.application.DocService;
import com.decade.nexa.documents.application.ports.in.SearchService;
import com.decade.nexa.documents.application.ports.out.SuggestService;
import com.decade.nexa.documents.domain.DocType;
import com.decade.nexa.documents.dto.*;
import com.decade.nexa.files.apis.FileIntegrityException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/docs")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocService docService;
    private final SearchService searchService;
    private final SuggestService suggestService;

    @ExceptionHandler(FileIntegrityException.class)
    ProblemDetail handle(FileIntegrityException exception) {
        log.warn("File integrity error", exception);
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("File integrity error");
        pd.setDetail("Mismatch Etag");
        return pd;
    }


    @Operation(description = "Add a new document", responses = {
        @ApiResponse(responseCode = "202", description = "Document added successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failure", content = @Content(
            mediaType = "application/problem+json",
            schema = @Schema(implementation = ProblemDetail.class),
            examples = {
                @ExampleObject(name = "Request validation", ref = "#/components/examples/Validation"),
                @ExampleObject(name = "File integrity violated", value = """
                    
                      {
                            "title": "File integrity error",
                            "status": 400,
                            "detail": "Mismatch Etag",
                            "instance": "/docs/add"
                      }
                    """)
            }
        ))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    void add(@Valid @RequestBody CreateDocumentRequest request) throws FileIntegrityException {
        docService.add(request);
    }

    @Operation(description = "Find a document by id", responses = {
        @ApiResponse(responseCode = "200", description = "The document"),
        @ApiResponse(responseCode = "404", description = "Document not found", content = @Content(
            mediaType = "application/problem+json",
            schema = @Schema(implementation = ProblemDetail.class),
            examples = {@ExampleObject(ref = "#/components/examples/NotFound")}
        ))
    })
    @GetMapping("/{id}")
    DocumentResponse find(@PathVariable String id) {
        return docService.find(id);
    }

    @Operation(description = "Search documents", responses = {
        @ApiResponse(responseCode = "200", description = "The documents"),
        @ApiResponse(responseCode = "400", description = "Validation failure", content = @Content(
            mediaType = "application/problem+json",
            schema = @Schema(implementation = ProblemDetail.class),
            examples = {@ExampleObject(ref = "#/components/examples/Validation")}
        ))
    })
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


    @Operation(description = "Suggest documents", responses = {
        @ApiResponse(responseCode = "200", description = "The suggestion text stream", content = @Content(
            mediaType = "text/plain",
            examples = {@ExampleObject(value = """
                      Hello, this is teri aka decade
                """)}
        )),
    })
    @PostMapping(path = "suggest", produces = MediaType.TEXT_PLAIN_VALUE)
    Flux<String> suggest(@RequestParam String query) {
        return suggestService.suggest(query);
    }
}
