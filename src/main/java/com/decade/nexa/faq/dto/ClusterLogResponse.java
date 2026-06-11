package com.decade.nexa.faq.dto;

import com.decade.nexa.faq.domain.LogStatus;

import java.time.LocalDate;

public record ClusterLogResponse(
    Long id,
    LocalDate date,
    LogStatus status,
    String message) {
}
