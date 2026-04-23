package com.decade.nexa.files.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PresignedDownloadResponse {
    private String fileKey;
    private String presignedDownloadUrl;
}
