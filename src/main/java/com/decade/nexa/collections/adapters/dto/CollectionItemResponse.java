package com.decade.nexa.collections.adapters.dto;

import java.time.LocalDate;

public record CollectionItemResponse(
    String docId,
    String title,
    String filename,
    LocalDate addedAt
) {
}
