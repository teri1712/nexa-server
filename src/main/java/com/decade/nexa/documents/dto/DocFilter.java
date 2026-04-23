package com.decade.nexa.documents.dto;

import com.decade.nexa.documents.domain.DocType;

import java.time.Instant;

public record DocFilter(
          String query,
          Instant start,
          Instant end,
          DocType type,
          LastDoc lastDoc
) {
}
