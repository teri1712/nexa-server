package com.decade.nexa.documents.dto;

import java.util.List;

public record DocPage(long totalPages, List<DocumentItemResponse> docs) {
}
