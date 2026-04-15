package com.decade.nexa.documents.dto;

import com.decade.nexa.documents.domain.DocType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class DocumentItemResponse {

      private String id;
      private String filename;
      private String title;
      private DocType fileType;
      private Instant createdAt;

      private float score;

}
