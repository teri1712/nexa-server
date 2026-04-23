package com.decade.nexa.files.application;

import com.decade.nexa.files.application.ports.out.StoragePathGenerator;
import com.decade.nexa.files.dto.S3PresignedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PresignedUrlService {

      private final StoragePathGenerator pathGenerator;


      public S3PresignedResponse generateUploadUrl(String filename, String username) {

            StoragePathGenerator.Path generation = pathGenerator.generateUpload(username, filename);
            String key = generation.key();

            return S3PresignedResponse.builder()
                      .fileKey(key)
                      .presignedUploadUrl(generation.url())
                      .build();
      }

}
