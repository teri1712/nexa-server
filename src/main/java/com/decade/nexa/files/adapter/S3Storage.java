package com.decade.nexa.files.adapter;

import com.decade.nexa.files.apis.FileIntegrityException;
import com.decade.nexa.files.application.ports.out.FileStorage;
import com.decade.nexa.files.application.ports.out.StoragePathGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class S3Storage implements StoragePathGenerator, FileStorage {

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
    public Path generateDownload(String username, String fileKey) {
        GetObjectRequest putObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(fileKey)
            .build();

        GetObjectPresignRequest presignRequest =
            GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(putObjectRequest)
                .build();

        String url = presigner.presignGetObject(presignRequest)
            .url()
            .toString();
        return new Path(fileKey, url);
    }

    @Override
    public Map<String, String> getFile(String fileKey, String eTag) throws FileIntegrityException {

        HeadObjectRequest request = HeadObjectRequest.builder()
            .bucket(bucket)
            .key(fileKey)
            .build();

        HeadObjectResponse response = s3Client.headObject(request);
        if (response.eTag() == null || !response.eTag().equals(eTag)) {
            throw new FileIntegrityException(fileKey, bucket, eTag, response.eTag());
        }
        return response.metadata();
    }

    @Override
    public Resource getResource(String fileKey) {

        GetObjectRequest request = GetObjectRequest.builder()
            .bucket(bucket)
            .key(fileKey)
            .build();

        ResponseInputStream<GetObjectResponse> response =
            s3Client.getObject(request);
        return new InputStreamResource(response);
    }
}
