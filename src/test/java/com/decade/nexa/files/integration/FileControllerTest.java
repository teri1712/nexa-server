package com.decade.nexa.files.integration;

import com.decade.nexa.common.jwt.WithJwtUser;
import com.decade.nexa.files.dto.PresignedDownloadResponse;
import com.decade.nexa.files.dto.PresignedUploadResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
public class FileControllerTest extends FileModuleTest {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Test
    @WithJwtUser
    void givenServerReturnPresignUpload_mustBeAbleToUploadFileWithThatUrl() throws Exception {
        String filename = "test-upload.txt";
        String content = "Hello presigned upload!";

        // 1. Get presigned upload URL
        String responseStr = mvc.perform(post("/files/upload")
                        .param("filename", filename))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PresignedUploadResponse uploadResponse = objectMapper.readValue(responseStr, PresignedUploadResponse.class);
        String uploadUrl = uploadResponse.getPresignedUploadUrl();
        String fileKey = uploadResponse.getFileKey();

        assertThat(uploadUrl).isNotEmpty();
        assertThat(fileKey).isNotEmpty();

        // 2. Perform HTTP PUT to uploadUrl using java.net.http.HttpClient
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest uploadRequest = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Content-Type", "application/octet-stream")
                .PUT(HttpRequest.BodyPublishers.ofString(content))
                .build();

        HttpResponse<String> uploadResult = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(uploadResult.statusCode()).isEqualTo(200);

        // 3. Verify the file exists in S3 via s3Client
        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build())) {
            String s3Content = new String(s3Object.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(s3Content).isEqualTo(content);
        }
    }

    @Test
    @WithJwtUser
    void givenFileAlrUploadedAndWeHaveFileKey_whenUserGetDownloadUrl_thenMustReturnUrl() throws Exception {
        String fileKey = "alice_test-download.txt";
        String content = "Hello presigned download!";

        // Upload the file first to S3
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .contentType("application/octet-stream")
                .build(), software.amazon.awssdk.core.sync.RequestBody.fromString(content));

        // Get download URL
        String responseStr = mvc.perform(post("/files/download")
                        .param("filekey", fileKey))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PresignedDownloadResponse downloadResponse = objectMapper.readValue(responseStr, PresignedDownloadResponse.class);
        assertThat(downloadResponse.getPresignedDownloadUrl()).isNotEmpty();
        assertThat(downloadResponse.getFileKey()).isEqualTo(fileKey);
    }

    @Test
    @WithJwtUser
    void givenDownloadUrlIsReturned_mustBeAbleToDownloadWithThatUrl() throws Exception {
        String fileKey = "alice_test-download-2.txt";
        String content = "Hello direct download!";

        // Upload the file first to S3
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .contentType("application/octet-stream")
                .build(), software.amazon.awssdk.core.sync.RequestBody.fromString(content));

        // Get download URL
        String responseStr = mvc.perform(post("/files/download")
                        .param("filekey", fileKey))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PresignedDownloadResponse downloadResponse = objectMapper.readValue(responseStr, PresignedDownloadResponse.class);
        String downloadUrl = downloadResponse.getPresignedDownloadUrl();

        // Download using HttpClient
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest downloadRequest = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .GET()
                .build();

        HttpResponse<String> downloadResult = client.send(downloadRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(downloadResult.statusCode()).isEqualTo(200);
        assertThat(downloadResult.body()).isEqualTo(content);
    }
}
