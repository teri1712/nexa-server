package com.decade.nexa.collections.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("collection_items")
public record CollectionItem(
    @Id
    Long id,

    @Column("collection_id")
    Long collectionId,

    @Column("document_id")
    String documentId,

    @Column("added_at")
    LocalDate addedAt
) {
}
