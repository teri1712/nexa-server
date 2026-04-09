package com.decade.nexa.files.adapter;

import com.decade.nexa.files.apis.FileApi;
import com.decade.nexa.files.apis.FileResource;
import com.decade.nexa.files.application.ports.out.StoragePathGenerator;
import com.decade.nexa.files.domain.FileIntegrityException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class S3Storage implements StoragePathGenerator, FileApi {

      private final S3Presigner presigner;
      private final S3Client s3Client;

      @Value("${aws.s3.bucket}")
      private String bucket;

      @Value("${aws.s3.endpoint}")
      private String s3Endpoint;

      private String generateKey(String username, String filename) {
            return username + "/" + filename;
      }

      @Override
      public Path generateUpload(String username, String filename) {
            String key = generateKey(username, filename);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                      .bucket(bucket)
                      .key(key)
                      .contentType("application/octet-stream")
                      .build();

            PutObjectPresignRequest presignRequest =
                      PutObjectPresignRequest.builder()
                                .signatureDuration(Duration.ofMinutes(5))
                                .putObjectRequest(putObjectRequest)
                                .build();

            String url = presigner.presignPutObject(presignRequest)
                      .url()
                      .toString();
            return new Path(key, url);
      }

      @Override
      public void validate(String key, String eTag) throws FileIntegrityException {
            String expectedEtag = getEtag(key);
            if (eTag == null || !eTag.equals(expectedEtag)) {
                  throw new FileIntegrityException(key, bucket, expectedEtag, eTag);
            }
      }

      public String generateDownload(String key) {
            String[] parts = key.split("/");
            String username = parts[0];
            String filename = parts[1];
            return s3Endpoint + "/" + bucket + "/" + UriUtils.encodePath(username, StandardCharsets.UTF_8) + "/" + UriUtils.encodePath(filename, StandardCharsets.UTF_8);
      }

      private String getEtag(String key) {
            HeadObjectResponse res = s3Client.headObject(
                      HeadObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .build()
            );
            return res.eTag();
      }

      @Override
      public FileResource getFile(String fileKey) {

            GetObjectRequest request = GetObjectRequest.builder()
                      .bucket(bucket)
                      .key(fileKey)
                      .build();

            ResponseInputStream<GetObjectResponse> response =
                      s3Client.getObject(request);
            Resource resource = new InputStreamResource(response);
            String fileType = response.response().contentType();
            String fileName = response.response().metadata().get("filename");

            return new FileResource(fileName, fileType, resource, generateDownload(fileKey));
      }
}
