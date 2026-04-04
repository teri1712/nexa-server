package com.decade.nexa.files.application.ports.out;

public interface StoragePathGenerator {

      Presigned generatePresignUpload(String username, String filename);

      String generateDownload(String key, String eTag);

      record Presigned(String key, String url) {
      }
}
