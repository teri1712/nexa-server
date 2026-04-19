package com.decade.nexa.files.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PresignedUploadResponse {
    private String fileKey;
    private String presignedUploadUrl;
}
