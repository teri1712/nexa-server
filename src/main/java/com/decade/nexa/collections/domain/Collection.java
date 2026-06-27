package com.decade.nexa.collections.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("collections")
public record Collection(
    @Id
    Long id,
    @Column("user_id")
    UUID userId,
    String name,

    @Column("parent_id")
    Long parentId
) {
}

