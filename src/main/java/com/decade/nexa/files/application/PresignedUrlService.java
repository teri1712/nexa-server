package com.decade.nexa.files.application;

import com.decade.nexa.files.application.ports.out.StoragePathGenerator;
import com.decade.nexa.files.dto.PresignedDownloadResponse;
import com.decade.nexa.files.dto.PresignedUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    private final StoragePathGenerator pathGenerator;


    public PresignedUploadResponse generateUploadUrl(String filename, String username) {

        StoragePathGenerator.Path generation = pathGenerator.generateUpload(username, filename);
        String key = generation.key();

        return PresignedUploadResponse.builder()
            .fileKey(key)
            .presignedUploadUrl(generation.url())
            .build();
    }

    public PresignedDownloadResponse generateDownloadUrl(String fileKey, String username) {

        StoragePathGenerator.Path generation = pathGenerator.generateDownload(username, fileKey);
        String key = generation.key();

        return PresignedDownloadResponse.builder()
            .fileKey(key)
            .presignedDownloadUrl(generation.url())
            .build();
    }

}
