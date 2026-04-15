package com.decade.nexa.files.application.ports.out;

public interface StoragePathGenerator {

      Path generateUpload(String username, String filename);

      record Path(String key, String url) {
      }
}
