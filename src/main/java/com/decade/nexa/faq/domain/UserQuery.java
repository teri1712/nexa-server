package com.decade.nexa.faq.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("user_query")
public record UserQuery(
    @Id
    Long id,
    String query,
    LocalDate createdAt
) {
}
