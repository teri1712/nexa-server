package com.decade.nexa.documents.dto;

import com.decade.nexa.documents.domain.DocType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDocumentRequest(
          @NotBlank
          String filename,
          @NotNull
          String eTag,
          @NotNull
          String fileKey,
          @NotBlank
          String title,
          @NotBlank
          String description,
          DocType type
) {
}
