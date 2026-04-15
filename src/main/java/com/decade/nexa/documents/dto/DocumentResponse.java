package com.decade.nexa.documents.dto;

import com.decade.nexa.documents.domain.DocType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class DocumentResponse {

      private String id;
      private String filename;
      private String title;
      private String description;
      private DocType fileType;
      private Instant createdAt;

      private float score;

}
