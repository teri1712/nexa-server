package com.decade.nexa.documents.dto;

import com.decade.nexa.documents.domain.LogStatus;

import java.time.LocalDate;

public record IndexLogResponse(
    Long id,
    LocalDate date,
    LogStatus status,
    String message) {
}
