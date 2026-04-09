package com.decade.nexa.files.application;

import com.decade.nexa.files.application.ports.out.RecordRepository;
import com.decade.nexa.files.application.ports.out.StoragePathGenerator;
import com.decade.nexa.files.domain.UploadRecord;
import com.decade.nexa.files.dto.CompleteUploadRequest;
import com.decade.nexa.files.dto.S3PresignedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PresignedUrlService {

      private final RecordRepository records;
      private final StoragePathGenerator pathGenerator;


      public S3PresignedResponse generateUploadUrl(String filename, String username) {

            StoragePathGenerator.Path generation = pathGenerator.generateUpload(username, filename);
            String key = generation.key();
            records.save(new UploadRecord(key));

            return S3PresignedResponse.builder()
                      .key(key)
                      .presignedUploadUrl(generation.url())
                      .filename(filename)
                      .build();
      }

      @Transactional
      public void finishUpload(CompleteUploadRequest request) {
            String key = request.key();
            UploadRecord record = records.findById(key).orElseThrow();
            record.complete();
            records.save(record);
      }

}
