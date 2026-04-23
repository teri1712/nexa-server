package com.decade.nexa.documents.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;

@Document(indexName = "nexa-documents")
@Getter
public class DocumentInsight {
      @Id
      @Field(type = FieldType.Keyword)
      private String id;

      @Field(type = FieldType.Object)
      private Map<String, Object> metadata;

      @Field(type = FieldType.Dense_Vector, dims = 768, similarity = "cosine")
      private float[] embedding;

      @Field(type = FieldType.Text)
      private String content;
}
