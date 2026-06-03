package com.decade.nexa.documents.domain;

import com.decade.nexa.documents.domain.events.DocCreated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

/**
 *
 */
@Document(indexName = "documentation")
@NoArgsConstructor
@Getter
public class Documentation extends AbstractAggregateRoot<Documentation> {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text)
    private String filename;

    @Field(type = FieldType.Text)
    private String title;
    @Field(type = FieldType.Text)
    private String description;

    @Field(name = "content_type", type = FieldType.Keyword)
    private DocType contentType;

    @Field(name = "created_at", type = FieldType.Date)
    private Instant createdAt;

    public Documentation(String id, String filename, String title, String description, DocType contentType) {
        this.contentType = contentType;
        this.description = description;
        this.filename = filename;
        this.id = id;
        this.title = title;
        this.createdAt = Instant.now();
        registerEvent(new DocCreated(id, contentType, createdAt));
    }

}
