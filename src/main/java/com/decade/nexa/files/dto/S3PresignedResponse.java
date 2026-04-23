package com.decade.nexa.files.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class S3PresignedResponse {
      private String fileKey;
      private String presignedUploadUrl;
}
